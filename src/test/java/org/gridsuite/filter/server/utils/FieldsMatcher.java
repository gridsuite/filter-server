/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.hamcrest.Description;
import org.hamcrest.StringDescription;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.collection.ArrayMatching;
import org.hamcrest.collection.IsIterableContainingInOrder;

import static java.lang.String.format;
import lombok.SneakyThrows;

/**
 * @author Laurent Garnier <laurent.garnier at rte-france.com>
 */

class EqualDiagnosingMatcher<U> extends TypeSafeDiagnosingMatcher<U> {

    private final U expected;

    EqualDiagnosingMatcher(U expected) {
        this.expected = expected;
    }

    protected boolean matchesSafely(U actual, Description mismatchDescription) {
        boolean equals = Objects.equals(expected, actual);
        if (!equals) {
            mismatchDescription.appendText("was not " + expected);
        }
        return equals;
    }

    public void describeTo(Description description) {
        description.appendText("Objects.equals");
    }
}

public class FieldsMatcher<T> extends TypeSafeDiagnosingMatcher<T> {

    private static final String SEP = "/";

    private static final Set<Class<?>> CHEAP_EQUAL_CLASSES = Set.of(String.class, Boolean.class,
        Integer.class, Float.class, Double.class, Long.class, Short.class,
        Enum.class);

    T expected;
    private Map<String, TypeSafeMatcher<?>>   pathBasedMatchersMap;
    private Map<Class<?>, TypeSafeMatcher<?>> classBasedMatcherMap;

    public FieldsMatcher(T expected,
        Map<String, TypeSafeMatcher<?>> pathBasedMatchersMap,
        Map<Class<?>, TypeSafeMatcher<?>> classBasedMatcherMap) {

        this.expected = expected;
        this.pathBasedMatchersMap = pathBasedMatchersMap;
        this.classBasedMatcherMap = classBasedMatcherMap;
    }

    public FieldsMatcher(T expected) {
        this(expected, null, null);
    }

    @SneakyThrows
    @Override
    protected boolean matchesSafely(T actual, Description mismatch) {
        return matchesRecurs(null, expected, SEP, actual, mismatch);
    }

    protected TypeSafeDiagnosingMatcher<T> getSloppyMatcher(String path, Object expected) {
        return null;
    }

    protected <U, V> TypeSafeDiagnosingMatcher<Map<U, V>> makeMapSloppyMatcher(Map<U, V> expected, String path) {
        return new TypeSafeDiagnosingMatcher<>() {
            protected boolean matchesSafely(Map<U, V> actual, Description mismatch) {
                Set<U> expectedKeySet = expected.keySet();
                Set<U> actualKeySet = actual.keySet();
                Collection<U> keysUnion = CollectionUtils.union(expectedKeySet, actualKeySet);
                Collection<U> intersection = CollectionUtils.intersection(expectedKeySet, actualKeySet);
                int mismatchCount = keysUnion.size() - intersection.size();
                if (mismatchCount != 0) {
                    Set<U> onlyExpected = new TreeSet<>(keysUnion);
                    onlyExpected.removeAll(actualKeySet);
                    Set<U> onlyActual = new TreeSet<>(keysUnion);
                    onlyActual.removeAll(expectedKeySet);
                    // tell problematic keys
                    mismatch.appendText("unfullfilled").appendValue(onlyExpected)
                        .appendText(" unexpected").appendValue(onlyActual);
                }

                for (U k : intersection) {
                    V expectedValue = expected.get(k);
                    V actualValue = actual.get(k);

                    boolean valueMatch = matchesRecurs(null, expectedValue, "{" + k + "}", actualValue, mismatch);
                    if (!valueMatch) {
                        mismatchCount += 1;
                    }
                }

                return mismatchCount == 0;
            }

            public void describeTo(Description description) {
                description.appendText("map without order on keys");
            }
        };
    }

    protected <U> TypeSafeDiagnosingMatcher<List<U>> makeListSloppyMatcher(List<U> expected, String path) {
        return new TypeSafeDiagnosingMatcher<>() {
            protected boolean matchesSafely(List<U> actual, Description mismatch) {
                int expectedSize = expected.size();
                int actualSize = actual.size();

                SortedMap<Integer, Integer> actualToExpectedIndices = new TreeMap<>();
                SortedMap<Integer, Integer> expectedToActualIndices = new TreeMap<>();
                for (int i = 0; i < expectedSize; i++) {
                    U expectedElement = expected.get(i);
                    boolean valueMatch = false;
                    int j = i;
                    while (!valueMatch && j < actualSize) {
                        U actualElement = actual.get(j);
                        valueMatch = matchesRecurs(null, expectedElement, path, actualElement, Description.NONE);
                        if (!valueMatch) {
                            j++;
                        } else {
                            actualToExpectedIndices.put(j, i);
                            expectedToActualIndices.put(i, j);
                        }
                    }
                }

                for (int j = 0; j < actualSize; j++) {
                    if (actualToExpectedIndices.containsKey(j)) {
                        continue;
                    }

                    U actualElement = actual.get(j);
                    boolean valueMatch = false;
                    int i = j + 1; // j would have been in actualToExpectedIndices otherwise
                    while (!valueMatch && i < expectedSize) {
                        if (expectedToActualIndices.containsKey(i)) {
                            i++;
                            continue;
                        }
                        U expectedElement = expected.get(i);
                        valueMatch = matchesRecurs(null, expectedElement, path, actualElement, Description.NONE);
                        if (!valueMatch) {
                            i++;
                        } else {
                            actualToExpectedIndices.put(j, i);
                            expectedToActualIndices.put(i, j);
                        }
                    }
                }

                int mismatchCount = tellMismatches(mismatch, actual, actualToExpectedIndices, expectedToActualIndices);
                return mismatchCount == 0;
            }

            private int tellMismatches(Description mismatch, List<U> actual,
                SortedMap<Integer, Integer> actualToExpectedIndices,
                SortedMap<Integer, Integer> expectedToActualIndices) {

                int mismatchCount = Math.abs(expected.size() - actual.size());
                if (mismatchCount != 0) {
                    mismatch.appendText("difference in size : expected " + expected.size() + " actual " + actual.size() + " ");
                }

                List<MutableTriple<Integer, Integer, Integer>> triples = new ArrayList<>();
                for (int i = 0; i < expected.size(); i++) {
                    Integer actualIndex = expectedToActualIndices.get(i);
                    if (actualIndex == null) {
                        mismatchCount += 1;
                        triples.add(MutableTriple.of(i, null, null));
                    } else if (actualIndex == i) {
                        triples.add(MutableTriple.of(i, 0, 0));
                    } else {
                        mismatchCount += 1;
                        int delta = i - actualIndex;
                        MutableTriple<Integer, Integer, Integer> last = triples.isEmpty() ? null : triples.get(triples.size() - 1);
                        if (last == null || last.getMiddle() == null || last.getMiddle() != delta) {
                            triples.add(MutableTriple.of(i, delta, 1));
                        } else {
                            last.setRight(last.getRight() + 1);
                        }
                    }
                }
                for (Triple<Integer, Integer, Integer> t : triples) {
                    Integer delta = t.getMiddle();
                    if (delta == null) {
                        mismatch.appendText("[" + t.getLeft() + "!]");
                    } else if (delta != 0) {
                        int expectedIndex = t.getLeft();
                        int runLength = t.getRight();
                        int actualIndex = expectedIndex - delta;
                        mismatch.appendText("[" + expectedIndex + "->" + actualIndex + "]");
                        if (runLength > 2) {
                            mismatch.appendText(" -- ");
                        }
                        if (runLength > 1) {
                            int lastExpectedIndex = expectedIndex + runLength - 1;
                            mismatch.appendText("[" + lastExpectedIndex + "->" + (lastExpectedIndex - delta) + "]");
                        }
                    }
                }

                for (int j = 0; j < actual.size(); j++) {
                    Integer expectedIndex = actualToExpectedIndices.get(j);
                    if (expectedIndex == null) {
                        mismatch.appendText("[!" + j + "]");
                        mismatchCount += 1;
                    }
                }

                return mismatchCount;
            }

            public void describeTo(Description description) {
                description.appendText("ordered");
            }
        };
    }

    protected <U> TypeSafeDiagnosingMatcher<Collection<U>> makeCollectionSloppyMatcher(Collection<U> expected, String path) {
        return new TypeSafeDiagnosingMatcher<>() {
            protected boolean matchesSafely(Collection<U> actual, Description mismatch) {
                int mismatchCount = Math.abs(expected.size() - actual.size());

                LinkedList<U> shrinkingActuals = new LinkedList<>(actual);
                List<U> unfulfilled = new ArrayList<>();
                for (U expectedElement : expected) {
                    ListIterator<U> actualIterator = shrinkingActuals.listIterator();
                    ListIterator<U> foundIt = null;
                    while (foundIt == null && actualIterator.hasNext()) {
                        U actualElement = actualIterator.next();
                        boolean valueMatch = matchesRecurs(null, expectedElement, "*", actualElement, Description.NONE);
                        if (valueMatch) {
                            foundIt = actualIterator;
                            actualIterator.remove();
                        }
                    }
                    if (foundIt == null) {
                        unfulfilled.add(expectedElement);
                    }
                }
                if (mismatchCount + unfulfilled.size() + shrinkingActuals.size() > 0) {
                    mismatch.appendText("*");
                }
                if (mismatchCount != 0) {
                    mismatch.appendText(" difference in size : expected ").appendValue(expected.size())
                        .appendText(" actual ").appendValue(actual.size());
                }
                if (!unfulfilled.isEmpty()) {
                    mismatch.appendText(" unfulfilled").appendValue(unfulfilled);
                }
                if (!shrinkingActuals.isEmpty()) {
                    mismatch.appendText(" unexpected").appendValue(shrinkingActuals);
                }
                mismatchCount += shrinkingActuals.size() + unfulfilled.size();
                return mismatchCount == 0;
            }

            public void describeTo(Description description) {
                description.appendText("collection");
            }
        };
    }

    protected <S> TypeSafeDiagnosingMatcher<S> getSloppyMatcher(Class<? extends S> clazz, S expected, String path) {
        if (CHEAP_EQUAL_CLASSES.contains(clazz)) {
            return new EqualDiagnosingMatcher<S>(expected);
        }

        if (clazz == Map.class) {
            //noinspection unchecked,rawtypes
            return makeMapSloppyMatcher((Map) expected, path);
        }

        if (List.class.isAssignableFrom(clazz)) {
            //noinspection unchecked,rawtypes
            return makeListSloppyMatcher((List) expected, path);
        }

        if (Collection.class.isAssignableFrom(clazz)) {
            //noinspection unchecked,rawtypes
            return makeCollectionSloppyMatcher((Collection) expected, path);
        }

        if (Iterable.class.isAssignableFrom(clazz)) {
            //noinspection unchecked
            return (TypeSafeDiagnosingMatcher<S>)
                new IsIterableContainingInOrder<>(ArrayMatching.asEqualMatchers(((Set<S>) expected).toArray()));
        }
        return null;
    }

    private static <T> Boolean mitigates(TypeSafeDiagnosingMatcher<?> mitigater,
        T expected, String path, T actual, Description mismatch) {

        assert expected != actual; // implies at least one is NOT null

        if (mitigater != null && mitigater.matches(actual)) {
            return true;
        } else if (mitigater != null) {
            mismatch.appendText(path + " ");
            mitigater.describeMismatch(actual, mismatch);
            return false;
        } else if (expected == null) {
            mismatch.appendText(path + " " + "was not null");
            return false;
        }

        return null;
    }

    private static List<Field> getFieldsUpTo(@Nonnull Class<?> startClass, @Nullable Class<?> exclusiveParent) {
        List<Field> classFields = new ArrayList<>();
        addFieldsTo(startClass, exclusiveParent, classFields);
        return classFields;
    }

    private static void addFieldsTo(@Nonnull Class<?> startClass, @Nullable Class<?> exclusiveParent, List<Field> fields) {
        List<Field> declaredField = Arrays.asList(startClass.getDeclaredFields());
        declaredField.forEach(f -> f.setAccessible(true));
        fields.addAll(declaredField);
        Class<?> parentClass = startClass.getSuperclass();

        if (parentClass != null && !parentClass.equals(exclusiveParent)) {
            addFieldsTo(parentClass, exclusiveParent, fields);
        }
    }

    protected <S> boolean matchesRecurs(Class<?> clazz, S expected, String path, S actual, Description mismatch) {
        if (expected == actual) {
            return true;
        }

        TypeSafeDiagnosingMatcher<?> mitigater = getSloppyMatcher(path, expected);
        Boolean resolves = mitigates(mitigater, expected, path, actual, mismatch);
        if (resolves != null) {
            return resolves;
        }

        Class<?> expectedClass = clazz != null ? clazz : expected.getClass();
        mitigater = getSloppyMatcher(expectedClass, expected, path);
        resolves = mitigates(mitigater, expected, path, actual, mismatch);
        if (resolves != null) {
            return resolves;
        }

        if (!expectedClass.isInstance(actual)) {
            if (actual == null) {
                mismatch.appendText(path + " was null");
            } else {
                mismatch.appendText(path + " was ")
                    .appendText(actual.getClass().getSimpleName())
                    .appendText(" ")
                    .appendValue(actual);
            }
            return false;
        }

        Description localDescription = new StringDescription();
        int mismatchCount = 0;
        List<Field> fields = getFieldsUpTo(expectedClass, null);
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            final Object subActual = uncheckedGet(field, actual);
            final Object subExpected = uncheckedGet(field, expected);
            String subPath = path + field.getName() + SEP;

            Class<?> subClazz = field.getType();

            boolean localMatch = matchesRecurs(subClazz, subExpected, subPath, subActual, localDescription);
            if (!localMatch) {
                mismatchCount += 1;
            }
        }

        boolean returnValue;
        if (0 != mismatchCount) {
            // localDescription should not be empty
            returnValue = false;
        } else if (fields.isEmpty()) {
            // uh uh, actual != expect but have no field comparable : better to alert
            mismatch.appendText(path + " can not compare non identical").appendValue(expected)
                .appendText("and").appendValue(actual);
            returnValue = false;
        } else {
            returnValue = true;
        }
        if (!returnValue) {
            mismatch.appendText(localDescription.toString());
        }

        return returnValue;
    }

    private static <U> U uncheckedGet(Field field, Object object) {
        try {
            //noinspection unchecked
            return (U) field.get(object);
        } catch (ClassCastException e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionError(format("IllegalAccess, reading field '%s' from %s", field.getName(), object));
        }
    }

    public void describeTo(Description description) {
        description.appendText("acyclic fields matcher");
    }
}
