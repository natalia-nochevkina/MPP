import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.withLock
import kotlin.concurrent.write
import kotlin.math.max
import kotlin.math.min

/**
 * Bank implementation.
 *
 * @author Nochevkina Natalia
 */
class BankImpl(n: Int) : Bank {
    private val accounts: Array<Account> = Array(n) { Account() }

    override val numberOfAccounts: Int
        get() = accounts.size

    override fun getAmount(index: Int): Long {
        val account = accounts[index]
        return account.lock.read { account.amount }
    }

    override val totalAmount: Long
        get() {
            accounts.map { it.lock.readLock().lock() }
            val res = accounts.sumOf { it.amount }
            accounts.map { it.lock.readLock().unlock() }
            return res
        }

    override fun deposit(index: Int, amount: Long): Long {
        require(amount > 0) { "Invalid amount: $amount" }
        val account = accounts[index]
        return account.lock.write {
            check(amount <= Bank.MAX_AMOUNT && account.amount + amount <= Bank.MAX_AMOUNT) { "Overflow" }
            account.amount += amount
            account.amount
        }
    }

    override fun withdraw(index: Int, amount: Long): Long {
        require(amount > 0) { "Invalid amount: $amount" }
        val account = accounts[index]
        return account.lock.write {
            check(account.amount - amount >= 0) { "Underflow" }
            account.amount -= amount
            account.amount
        }
    }

    override fun transfer(fromIndex: Int, toIndex: Int, amount: Long) {
        require(amount > 0) { "Invalid amount: $amount" }
        require(fromIndex != toIndex) { "fromIndex == toIndex" }
        val from = accounts[fromIndex]
        val to = accounts[toIndex]
        accounts[min(fromIndex,toIndex)].lock.write {
            accounts[max(fromIndex,toIndex)].lock.write {
                check(amount <= from.amount) { "Underflow" }
                check(amount <= Bank.MAX_AMOUNT && to.amount + amount <= Bank.MAX_AMOUNT) { "Overflow" }
                from.amount -= amount
                to.amount += amount
            }
        }
    }

    override fun consolidate(fromIndices: List<Int>, toIndex: Int) {
        require(fromIndices.isNotEmpty()) { "empty fromIndices" }
        require(fromIndices.distinct() == fromIndices) { "duplicates in fromIndices" }
        require(toIndex !in fromIndices) { "toIndex in fromIndices" }
        val fromList = fromIndices.plus(toIndex).sorted().map { accounts[it] }
        val to = accounts[toIndex]
        fromList.map { it.lock.writeLock().lock() }
        try {
            val amount = fromList.sumOf { it.amount }
            check(to.amount + amount <= Bank.MAX_AMOUNT) { "Overflow" }
            fromList.map { it.amount = 0 }
            to.amount += amount
        } finally {
            fromList.map { it.lock.writeLock().unlock() }
        }
    }

    /**
     * Private account data structure.
     */
    class Account {
        /**
         * Amount of funds in this account.
         */
        val lock = ReentrantReadWriteLock()
        var amount: Long = 0
    }
}