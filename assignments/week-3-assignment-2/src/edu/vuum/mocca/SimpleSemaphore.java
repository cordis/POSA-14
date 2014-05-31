package edu.vuum.mocca;

import java.util.concurrent.locks.*;

/**
 * @class SimpleSemaphore
 * 
 * @brief This class provides a simple counting semaphore
 *        implementation using Java a ReentrantLock and a
 *        ConditionObject (which is accessed via a Condition). It must
 *        implement both "Fair" and "NonFair" semaphore semantics,
 *        just liked Java Semaphores.
 */
public class SimpleSemaphore
{
    /**
     * Define a ReentrantLock to protect the critical section.
     */
    private final ReadWriteLock mLock;

    /**
     * Define a Condition that waits while the number of permits is 0.
     */
    // TODO - you fill in here
    private final Condition mHasResource;

    /**
     * Define a count of the number of available permits.
     */
    // TODO - you fill in here.  Make sure that this data member will
    // ensure its values aren't cached by multiple Threads..
    private int mResourceCount;

    private interface IInterruptStrategy
    {
        public void lock(Lock lock) throws InterruptedException;

        public void await(Condition condition) throws InterruptedException;
    }

    private IInterruptStrategy mInterruptibleStrategy = new IInterruptStrategy()
    {
        @Override
        public void lock(Lock lock) throws InterruptedException {
            lock.lockInterruptibly();
        }

        @Override
        public void await(Condition condition) throws InterruptedException {
            condition.await();
        }
    };

    private IInterruptStrategy mNonInterruptableStrategy = new IInterruptStrategy()
    {
        @Override
        public void lock(Lock lock) throws InterruptedException {
            lock.lock();
        }

        @Override
        public void await(Condition condition) throws InterruptedException {
            condition.awaitUninterruptibly();
        }
    };

    public SimpleSemaphore(int permits, boolean fair)
    {
        // TODO - you fill in here to initialize the SimpleSemaphore,
        // making sure to allow both fair and non-fair Semaphore
        // semantics.
        mResourceCount = permits;
        mLock = new ReentrantReadWriteLock(fair);
        mHasResource = mLock.writeLock().newCondition();
    }

    /**
     * Acquire one permit from the semaphore in a manner that can be
     * interrupted.
     */
    public void acquire() throws InterruptedException
    {
        // TODO - you fill in here.
        acquireWithStrategy(mInterruptibleStrategy);
    }

    /**
     * Acquire one permit from the semaphore in a manner that cannot be
     * interrupted.
     */
    public void acquireUninterruptibly()
    {
        // TODO - you fill in here.
        try {
            acquireWithStrategy(mNonInterruptableStrategy);
        }
        catch (InterruptedException e) {
            // pass: Never get here
        }
    }

    private void acquireWithStrategy(IInterruptStrategy strategy) throws InterruptedException
    {
        strategy.lock(mLock.writeLock());
        try {
            while (0 == mResourceCount) {
                strategy.await(mHasResource);
            }
            mResourceCount -= 1;
        }
        finally {
            mLock.writeLock().unlock();
        }
    }

    /**
     * Return one permit to the semaphore.
     */
    void release()
    {
        // TODO - you fill in here.
        mLock.writeLock().lock();
        mResourceCount += 1;
        mHasResource.signal();
        mLock.writeLock().unlock();
    }

    /**
     * Return the number of permits available.
     */
    public int availablePermits()
    {
        // TODO - you fill in here by changing null to the appropriate
        // return value.
        mLock.readLock().lock();
        try {
            return mResourceCount;
        }
        finally {
            mLock.readLock().unlock();
        }
    }

}
