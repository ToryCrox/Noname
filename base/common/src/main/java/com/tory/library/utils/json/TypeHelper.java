package com.tory.library.utils.json;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

public class TypeHelper {
    static final Type[] EMPTY_TYPE_ARRAY = new Type[0];

    /**
     * 获取List的泛型类
     */
    @NonNull
    public static <T> Type getListType(@NonNull Class<T> clazz) {
        return getParameterized(List.class, clazz);
    }

    /**
     * 获取 Map的泛型类
     *
     * @return
     */
    @NonNull
    public static <K, V> Type getMapType(@NonNull Class<K> kClazz, Class<V> vClass) {
        return getParameterized(Map.class, kClazz, vClass);
    }

    /**
     * 获取参数化Type，例如 GsonHelper.getParameterized(Map.class, String.class, String.class)
     *
     * @param rawType
     * @param typeArguments
     * @return
     */
    @NonNull
    public static Type getParameterized(@NonNull Type rawType, Type... typeArguments) {
        return new ParameterizedTypeImpl(null, rawType, typeArguments);
    }

    /**
     * Returns true if {@code a} and {@code b} are equal.
     */
    static boolean equals(Type a, Type b) {
        if (a == b) {
            return true; // Also handles (a == null && b == null).

        } else if (a instanceof Class) {
            return a.equals(b); // Class already specifies equals().

        } else if (a instanceof ParameterizedType) {
            if (!(b instanceof ParameterizedType)) return false;
            ParameterizedType pa = (ParameterizedType) a;
            ParameterizedType pb = (ParameterizedType) b;
            Object ownerA = pa.getOwnerType();
            Object ownerB = pb.getOwnerType();
            return (ownerA == ownerB || (ownerA != null && ownerA.equals(ownerB)))
                    && pa.getRawType().equals(pb.getRawType())
                    && Arrays.equals(pa.getActualTypeArguments(), pb.getActualTypeArguments());

        } else if (a instanceof GenericArrayType) {
            if (!(b instanceof GenericArrayType)) return false;
            GenericArrayType ga = (GenericArrayType) a;
            GenericArrayType gb = (GenericArrayType) b;
            return equals(ga.getGenericComponentType(), gb.getGenericComponentType());

        } else if (a instanceof WildcardType) {
            if (!(b instanceof WildcardType)) return false;
            WildcardType wa = (WildcardType) a;
            WildcardType wb = (WildcardType) b;
            return Arrays.equals(wa.getUpperBounds(), wb.getUpperBounds())
                    && Arrays.equals(wa.getLowerBounds(), wb.getLowerBounds());

        } else if (a instanceof TypeVariable) {
            if (!(b instanceof TypeVariable)) return false;
            TypeVariable<?> va = (TypeVariable<?>) a;
            TypeVariable<?> vb = (TypeVariable<?>) b;
            return va.getGenericDeclaration() == vb.getGenericDeclaration()
                    && va.getName().equals(vb.getName());

        } else {
            return false; // This isn't a type we support!
        }
    }

    /**
     * Returns a type that is functionally equal but not necessarily equal
     * according to {@link Object#equals(Object) Object.equals()}. The returned
     * type is {@link java.io.Serializable}.
     */
    public static Type canonicalize(Type type) {
        if (type instanceof Class) {
            Class<?> c = (Class<?>) type;
            return c.isArray() ? new GenericArrayTypeImpl(canonicalize(c.getComponentType())) : c;

        } else if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) type;
            return new ParameterizedTypeImpl(p.getOwnerType(),
                    p.getRawType(), p.getActualTypeArguments());

        } else if (type instanceof GenericArrayType) {
            GenericArrayType g = (GenericArrayType) type;
            return new GenericArrayTypeImpl(g.getGenericComponentType());

        } else if (type instanceof WildcardType) {
            WildcardType w = (WildcardType) type;
            return new WildcardTypeImpl(w.getUpperBounds(), w.getLowerBounds());

        } else {
            // type is either serializable as-is or unsupported
            return type;
        }
    }


    /**
     * Returns the generic supertype for {@code supertype}. For example, given a class {@code
     * IntegerSet}, the result for when supertype is {@code Set.class} is {@code Set<Integer>} and the
     * result when the supertype is {@code Collection.class} is {@code Collection<Integer>}.
     */
    static Type getGenericSupertype(Type context, Class<?> rawType, Class<?> toResolve) {
        if (toResolve == rawType) return context;

        // We skip searching through interfaces if unknown is an interface.
        if (toResolve.isInterface()) {
            Class<?>[] interfaces = rawType.getInterfaces();
            for (int i = 0, length = interfaces.length; i < length; i++) {
                if (interfaces[i] == toResolve) {
                    return rawType.getGenericInterfaces()[i];
                } else if (toResolve.isAssignableFrom(interfaces[i])) {
                    return getGenericSupertype(rawType.getGenericInterfaces()[i], interfaces[i], toResolve);
                }
            }
        }

        // Check our supertypes.
        if (!rawType.isInterface()) {
            while (rawType != Object.class) {
                Class<?> rawSupertype = rawType.getSuperclass();
                if (rawSupertype == toResolve) {
                    return rawType.getGenericSuperclass();
                } else if (toResolve.isAssignableFrom(rawSupertype)) {
                    return getGenericSupertype(rawType.getGenericSuperclass(), rawSupertype, toResolve);
                }
                rawType = rawSupertype;
            }
        }

        // We can't resolve this further.
        return toResolve;
    }

    private static int indexOf(Object[] array, Object toFind) {
        for (int i = 0; i < array.length; i++) {
            if (toFind.equals(array[i])) return i;
        }
        throw new NoSuchElementException();
    }

    static String typeToString(Type type) {
        return type instanceof Class ? ((Class<?>) type).getName() : type.toString();
    }

    public static ParameterizedType getSuperParameterizedType(Type context, Class<?> contextRawType, Class<?> supertype) {
        return (ParameterizedType) getSupertype(context, contextRawType, supertype);
    }

    /**
     * 获取目标的泛型类型
     *
     * @param contextRawType
     * @param supertype
     * @return
     */
    @NonNull
    public static ParameterizedType getSuperParameterizedType(@NonNull Class<?> contextRawType, @NonNull Class<?> supertype) {
        return (ParameterizedType) getSupertype(canonicalize(contextRawType), contextRawType, supertype);
    }

    /**
     * 获取继承supertype的泛型
     * Returns the generic form of {@code supertype}. For example, if this is {@code
     * ArrayList<String>}, this returns {@code Iterable<String>} given the input {@code
     * Iterable.class}.
     *
     * @param supertype a superclass of, or interface implemented by, this.
     */
    public static Type getSupertype(Type context, Class<?> contextRawType, Class<?> supertype) {
        if (!supertype.isAssignableFrom(contextRawType))
            throw new IllegalArgumentException("supertype:" + supertype.getName()
                    + " is not super class of contextRawType:" + contextRawType);
        return resolve(context, contextRawType,
                getGenericSupertype(context, contextRawType, supertype));
    }

    static Type resolve(Type context, Class<?> contextRawType, Type toResolve) {
        // This implementation is made a little more complicated in an attempt to avoid object-creation.
        while (true) {
            if (toResolve instanceof TypeVariable) {
                TypeVariable<?> typeVariable = (TypeVariable<?>) toResolve;
                toResolve = resolveTypeVariable(context, contextRawType, typeVariable);
                if (toResolve == typeVariable) {
                    return toResolve;
                }

            } else if (toResolve instanceof Class && ((Class<?>) toResolve).isArray()) {
                Class<?> original = (Class<?>) toResolve;
                Type componentType = original.getComponentType();
                Type newComponentType = resolve(context, contextRawType, componentType);
                return componentType == newComponentType ? original : new GenericArrayTypeImpl(
                        newComponentType);

            } else if (toResolve instanceof GenericArrayType) {
                GenericArrayType original = (GenericArrayType) toResolve;
                Type componentType = original.getGenericComponentType();
                Type newComponentType = resolve(context, contextRawType, componentType);
                return componentType == newComponentType ? original : new GenericArrayTypeImpl(
                        newComponentType);

            } else if (toResolve instanceof ParameterizedType) {
                ParameterizedType original = (ParameterizedType) toResolve;
                Type ownerType = original.getOwnerType();
                Type newOwnerType = resolve(context, contextRawType, ownerType);
                boolean changed = newOwnerType != ownerType;

                Type[] args = original.getActualTypeArguments();
                for (int t = 0, length = args.length; t < length; t++) {
                    Type resolvedTypeArgument = resolve(context, contextRawType, args[t]);
                    if (resolvedTypeArgument != args[t]) {
                        if (!changed) {
                            args = args.clone();
                            changed = true;
                        }
                        args[t] = resolvedTypeArgument;
                    }
                }

                return changed
                        ? new ParameterizedTypeImpl(newOwnerType, original.getRawType(), args)
                        : original;

            } else if (toResolve instanceof WildcardType) {
                WildcardType original = (WildcardType) toResolve;
                Type[] originalLowerBound = original.getLowerBounds();
                Type[] originalUpperBound = original.getUpperBounds();

                if (originalLowerBound.length == 1) {
                    Type lowerBound = resolve(context, contextRawType, originalLowerBound[0]);
                    if (lowerBound != originalLowerBound[0]) {
                        return new WildcardTypeImpl(new Type[]{Object.class}, new Type[]{lowerBound});
                    }
                } else if (originalUpperBound.length == 1) {
                    Type upperBound = resolve(context, contextRawType, originalUpperBound[0]);
                    if (upperBound != originalUpperBound[0]) {
                        return new WildcardTypeImpl(new Type[]{upperBound}, EMPTY_TYPE_ARRAY);
                    }
                }
                return original;

            } else {
                return toResolve;
            }
        }
    }

    private static Type resolveTypeVariable(
            Type context, Class<?> contextRawType, TypeVariable<?> unknown) {
        Class<?> declaredByRaw = declaringClassOf(unknown);

        // We can't reduce this further.
        if (declaredByRaw == null) return unknown;

        Type declaredBy = getGenericSupertype(context, contextRawType, declaredByRaw);
        if (declaredBy instanceof ParameterizedType) {
            int index = indexOf(declaredByRaw.getTypeParameters(), unknown);
            return ((ParameterizedType) declaredBy).getActualTypeArguments()[index];
        }

        return unknown;
    }

    /**
     * Returns the declaring class of {@code typeVariable}, or {@code null} if it was not declared by
     * a class.
     */
    private static @Nullable
    Class<?> declaringClassOf(TypeVariable<?> typeVariable) {
        GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();
        return genericDeclaration instanceof Class ? (Class<?>) genericDeclaration : null;
    }

    static void checkNotPrimitive(Type type) {
        if (type instanceof Class<?> && ((Class<?>) type).isPrimitive()) {
            throw new IllegalArgumentException();
        }
    }


    public static Type getParameterUpperBound(int index, ParameterizedType type) {
        Type[] types = type.getActualTypeArguments();
        if (index < 0 || index >= types.length) {
            throw new IllegalArgumentException(
                    "Index " + index + " not in range [0," + types.length + ") for " + type);
        }
        Type paramType = types[index];
        if (paramType instanceof WildcardType) {
            return ((WildcardType) paramType).getUpperBounds()[0];
        }
        return paramType;
    }

    public static Type getParameterLowerBound(int index, ParameterizedType type) {
        Type paramType = type.getActualTypeArguments()[index];
        if (paramType instanceof WildcardType) {
            return ((WildcardType) paramType).getLowerBounds()[0];
        }
        return paramType;
    }

    /**
     * 获取type所代表的class类
     * copy
     * @param type
     * @return
     */
    public static Class<?> getRawType(Type type) {
        if (type instanceof Class<?>) {
            // type is a normal class.
            return (Class<?>) type;

        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            // I'm not exactly sure why getRawType() returns Type instead of Class.
            // Neal isn't either but suspects some pathological case related
            // to nested classes exists.
            Type rawType = parameterizedType.getRawType();
            checkArgument(rawType instanceof Class);
            return (Class<?>) rawType;

        } else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType)type).getGenericComponentType();
            return Array.newInstance(getRawType(componentType), 0).getClass();

        } else if (type instanceof TypeVariable) {
            // we could use the variable's bounds, but that won't work if there are multiple.
            // having a raw type that's more general than necessary is okay
            return Object.class;

        } else if (type instanceof WildcardType) {
            return getRawType(((WildcardType) type).getUpperBounds()[0]);

        } else {
            String className = type == null ? "null" : type.getClass().getName();
            throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
                    + "GenericArrayType, but <" + type + "> is of type " + className);
        }
    }

    /**
     * 参数化类型
     */
    static final class ParameterizedTypeImpl implements ParameterizedType {
        private final @Nullable
        Type ownerType;
        private final Type rawType;
        private final Type[] typeArguments;

        ParameterizedTypeImpl(@Nullable Type ownerType, Type rawType, Type... typeArguments) {
            // Require an owner type if the raw type needs it.
            if (rawType instanceof Class<?>
                    && (ownerType == null) != (((Class<?>) rawType).getEnclosingClass() == null)) {
                throw new IllegalArgumentException();
            }

            for (Type typeArgument : typeArguments) {
                Objects.requireNonNull(typeArgument, "typeArgument == null");
                checkNotPrimitive(typeArgument);
            }

            this.ownerType = ownerType;
            this.rawType = rawType;
            this.typeArguments = typeArguments.clone();
        }

        @Override
        public Type[] getActualTypeArguments() {
            return typeArguments.clone();
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public @Nullable
        Type getOwnerType() {
            return ownerType;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof ParameterizedType && TypeHelper.equals(this, (ParameterizedType) other);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(typeArguments)
                    ^ rawType.hashCode()
                    ^ (ownerType != null ? ownerType.hashCode() : 0);
        }

        @Override
        public String toString() {
            if (typeArguments.length == 0) return typeToString(rawType);
            StringBuilder result = new StringBuilder(30 * (typeArguments.length + 1));
            result.append(typeToString(rawType));
            result.append("<").append(typeToString(typeArguments[0]));
            for (int i = 1; i < typeArguments.length; i++) {
                result.append(", ").append(typeToString(typeArguments[i]));
            }
            return result.append(">").toString();
        }
    }

    private static final class GenericArrayTypeImpl implements GenericArrayType {
        private final Type componentType;

        GenericArrayTypeImpl(Type componentType) {
            this.componentType = componentType;
        }

        @Override
        public Type getGenericComponentType() {
            return componentType;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof GenericArrayType
                    && TypeHelper.equals(this, (GenericArrayType) o);
        }

        @Override
        public int hashCode() {
            return componentType.hashCode();
        }

        @Override
        public String toString() {
            return typeToString(componentType) + "[]";
        }
    }

    /**
     * The WildcardType interface supports multiple upper bounds and multiple
     * lower bounds. We only support what the Java 6 language needs - at most one
     * bound. If a lower bound is set, the upper bound must be Object.class.
     */
    private static final class WildcardTypeImpl implements WildcardType {
        private final Type upperBound;
        private final Type lowerBound;

        WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
            if (lowerBounds.length > 1) throw new IllegalArgumentException();
            if (upperBounds.length != 1) throw new IllegalArgumentException();

            if (lowerBounds.length == 1) {
                if (lowerBounds[0] == null) throw new NullPointerException();
                checkNotPrimitive(lowerBounds[0]);
                if (upperBounds[0] != Object.class) throw new IllegalArgumentException();
                this.lowerBound = lowerBounds[0];
                this.upperBound = Object.class;
            } else {
                if (upperBounds[0] == null) throw new NullPointerException();
                checkNotPrimitive(upperBounds[0]);
                this.lowerBound = null;
                this.upperBound = upperBounds[0];
            }
        }

        @Override
        public Type[] getUpperBounds() {
            return new Type[]{upperBound};
        }

        @Override
        public Type[] getLowerBounds() {
            return lowerBound != null ? new Type[]{lowerBound} : EMPTY_TYPE_ARRAY;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof WildcardType && TypeHelper.equals(this, (WildcardType) other);
        }

        @Override
        public int hashCode() {
            // This equals Arrays.hashCode(getLowerBounds()) ^ Arrays.hashCode(getUpperBounds()).
            return (lowerBound != null ? 31 + lowerBound.hashCode() : 1) ^ (31 + upperBound.hashCode());
        }

        @Override
        public String toString() {
            if (lowerBound != null) return "? super " + typeToString(lowerBound);
            if (upperBound == Object.class) return "?";
            return "? extends " + typeToString(upperBound);
        }
    }

    public static void checkArgument(boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException();
        }
    }

}
