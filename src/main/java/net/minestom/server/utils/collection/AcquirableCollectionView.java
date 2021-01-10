package net.minestom.server.utils.collection;

import net.minestom.server.lock.Acquirable;
import net.minestom.server.lock.LockedElement;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class AcquirableCollectionView<E extends LockedElement> extends CollectionView<E, Acquirable<E>> {
    public AcquirableCollectionView(@NotNull Collection<Acquirable<E>> collectionView) {
        super(collectionView,
                LockedElement::getAcquiredElement,
                Acquirable::unsafeUnwrap);
    }
}
