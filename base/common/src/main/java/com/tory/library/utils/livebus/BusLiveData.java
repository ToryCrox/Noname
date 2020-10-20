package com.tory.library.utils.livebus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.Objects;

import static androidx.lifecycle.Lifecycle.State.DESTROYED;

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/10/14
 */
public class BusLiveData<T> extends MutableLiveData<T> {

    private static final int VERSION_START = -1;

    private int activeVersion = VERSION_START;
    private ArrayMap<Observer<? super T>, BusObserver<? super T>> busObservers = new ArrayMap();

    @Override
    public void setValue(T value) {
        activeVersion++;
        super.setValue(value);
    }

    private BusObserver<? super T> createBusObserver(@NonNull Observer<? super T> observer, int latestVersion) {
        BusObserver<? super T> busObserver = busObservers.get(observer);
        if (busObserver == null) {
            busObserver = new BusObserver(observer, latestVersion);
            busObservers.put(observer, busObserver);
        } else {
            throw new IllegalArgumentException("Please not register same observer " + observer);
        }
        return busObserver;
    }

    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        if (owner.getLifecycle().getCurrentState() == DESTROYED) {
            return;
        }
        super.observe(owner, createBusObserver(observer, activeVersion));
    }

    public void observeSticky(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
        if (owner.getLifecycle().getCurrentState() == DESTROYED) {
            return;
        }
        super.observe(owner, createBusObserver(observer, VERSION_START));
    }

    @Override
    public void observeForever(@NonNull Observer<? super T> observer) {
        super.observeForever(createBusObserver(observer, activeVersion));
    }

    public void observeStickyForever(@NonNull Observer<T> observer) {
        super.observeForever(createBusObserver(observer, VERSION_START));
    }

    @Override
    public void removeObserver(@NonNull Observer<? super T> observer) {
        BusObserver<? super T> busObserver;
        if (observer instanceof BusObserver) {
            busObserver = (BusObserver)observer;
        } else {
            busObserver = busObservers.get(observer);
        }
        if (busObserver != null) {
            busObservers.remove(busObserver.realObserver);
            super.removeObserver(busObserver);
        }
    }

    private class BusObserver<M extends T> implements Observer<M> {
        private Observer<M> realObserver;
        private int lastVersion;

        public BusObserver(@NonNull Observer<M> realObserver, int lastVersion) {
            this.realObserver = realObserver;
            this.lastVersion = lastVersion;
        }

        @Override
        public void onChanged(M m) {
            if (activeVersion <= lastVersion) {
                return;
            }
            lastVersion = activeVersion;
            if (m != null) {
                realObserver.onChanged(m);
            }
        }
    }

    private class ObserverKey<K extends T> {
        private Observer<K> realObserver;
        private LifecycleOwner owner;

        public ObserverKey(@NonNull Observer<K> realObserver,@Nullable LifecycleOwner owner) {
            this.realObserver = realObserver;
            this.owner = owner;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ObserverKey<?> that = (ObserverKey<?>) o;
            return Objects.equals(realObserver, that.realObserver) &&
                    Objects.equals(owner, that.owner);
        }

        @Override
        public int hashCode() {
            return Objects.hash(realObserver, owner);
        }
    }

}
