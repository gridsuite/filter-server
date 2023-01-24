package org.gridsuite.filter.server.utils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import org.hamcrest.Description;
import org.hamcrest.StringDescription;
import org.hamcrest.core.IsEqual;
import org.hamcrest.text.MatchesPattern;
import org.junit.Test;

import lombok.Builder;
import lombok.Getter;
import lombok.With;
import static org.hamcrest.MatcherAssert.assertThat;

public class FieldsMatcherMetaTest {

    public static final Top TOP_0 = Top.builder().str("une").i(1).bigint(BigInteger.TWO).bytes("3".getBytes(StandardCharsets.UTF_8))
        .orderedStrings(List.of("abc", "def"))
        .famousNumbers(Map.of("e", Math.E, "pi", Math.PI))
        .loto(Set.of(6, 2))
        .deep(Deep.builder().countries(Set.of("fr", "de")).deepers(Map.of("nonnull",
                Set.of(Deeper.builder().up(true).f(1.2f)
                    .build())))
            .build())
        .build();

    private void testInts(int actual, int pseudoExpected, String expectedDescription) {
        boolean realExpected = expectedDescription == null;
        FieldsMatcher<Integer> m = new FieldsMatcher<>(pseudoExpected);
        Description descr = new StringDescription();
        assertThat(m.matchesSafely(actual, descr), IsEqual.equalTo(realExpected));

        if (expectedDescription != null) {
            assertThat(descr.toString(), IsEqual.equalTo(expectedDescription));
        }
    }

    private <T> void testBasics(T actual, T pseudoExpected, String expectedDescription) {
        boolean realExpected = expectedDescription == null;
        FieldsMatcher<T> m = new FieldsMatcher<>(pseudoExpected);
        Description descr = new StringDescription();
        boolean got = m.matchesSafely(actual, descr);
        if (!got && expectedDescription == null) {
            System.err.println("Annoying " + descr);
        }
        assertThat(got, IsEqual.equalTo(realExpected));

        if (expectedDescription != null) {
            assertThat(descr.toString(), MatchesPattern.matchesPattern(expectedDescription));
        }
    }

    @Test
    public void unboxed() {
        testInts(Integer.MAX_VALUE, 0, "/ was not 0");
        testInts(0, Integer.MAX_VALUE, "/ was not 2147483647");
        testInts(0, 0, null);
    }

    @Test
    public void basics() {
        testBasics(null, "Pas null", "/ was null");
        testBasics("Nicht null", null, "/ was not null");
        testBasics(null, null, null);

        testBasics(1.95, 1.94, "/ was not 1.94");
        testBasics(1.95, 1.95, null);
    }

    @Builder
    @Getter
    @With
    static class Deeper {
        boolean up;
        Float f;
    }

    @Builder
    @Getter
    @With
    static class Deep {
        Set<String> countries;

        Map<String, Set<Deeper>> deepers;
    }

    @Builder
    @Getter
    @With
    static class Top {
        private String str;
        private int i;
        private byte[]       bytes;
        private BigInteger bigint;
        private List<String> orderedStrings;

        private Set<Integer> loto;
        private Map<String, Double> famousNumbers;

        private Deep deep;

        private Set<Deeper> deepers;
    }

    @Test
    public void selfIsSelf() {
        testBasics(TOP_0, TOP_0, null);
    }

    @Test
    public void shallowBasic() {
        testBasics(TOP_0.withI(2), TOP_0.withI(2), null);
        testBasics(TOP_0, TOP_0.withI(2), "/i/ was Integer <1>");
    }

    @Test
    public void shallowMap() {
        testBasics(TOP_0, TOP_0.withFamousNumbers(Map.of("e", Math.E, "pi", Math.PI)), null);
        testBasics(TOP_0, TOP_0.withFamousNumbers(Map.of("one", 1.0, "ZERO", 0.0)),
            "/famousNumbers/ unfullfilled<.*> unexpected<.*>");

    }

    @Test
    public void shallowSet() {
        testBasics(TOP_0.withLoto(Set.of(7, 8)), TOP_0.withLoto(Set.of(7, 8)), null);
        testBasics(TOP_0, TOP_0.withLoto(Set.of(7, 8)), "/loto/ \\* unfulfilled<.*> unexpected<.*>");
        testBasics(TOP_0, TOP_0.withLoto(Set.of(7)), "/loto/ \\* difference in size .* unfulfilled<.*> unexpected<.*>");

    }

    @Test
    public void shallowList() {
        testBasics(TOP_0.withOrderedStrings(List.of("def", "abc")), TOP_0.withOrderedStrings(List.of("def", "abc")), null);
        testBasics(TOP_0, TOP_0.withOrderedStrings(List.of("def", "abc")), "/orderedStrings/ \\[0->1\\]\\[1->0\\]");
        testBasics(TOP_0, TOP_0.withOrderedStrings(List.of()), "/orderedStrings/ .*in size.* .!0..!1.");
        testBasics(TOP_0.withOrderedStrings(List.of()), TOP_0, "/orderedStrings/ .*in size.* .0!..1!.");
        testBasics(TOP_0, TOP_0.withOrderedStrings(List.of("abc", "ghi")), "/orderedStrings/ .1!..!1.");
        testBasics(TOP_0.withOrderedStrings(List.of("xyz", "abc", "def", "ghi", "jkl")),
            TOP_0.withOrderedStrings(List.of("abc", "def", "ghi", "jkl", "mno")),
            "/orderedStrings/ .0->1. -- .3->4..4!..!0.");
    }

    @Test
    public void deepSet() {
        testBasics(TOP_0.withDeep(TOP_0.deep.withCountries(Set.of("ru", "uk"))),
            TOP_0.withDeep(TOP_0.deep.withCountries(Set.of("ru", "uk"))),
            null);
        testBasics(TOP_0.withDeep(TOP_0.deep.withCountries(Set.of("ru", "uk"))), TOP_0,
            "/deep/countries/ \\* unfulfilled<.*> unexpected<.*>");
    }

    @Test
    public void deeper() {
        testBasics(TOP_0.withDeep(TOP_0.deep.withDeepers(Map.of("what", Set.of()))),
            TOP_0.withDeep(TOP_0.deep.withDeepers(Map.of("what", Set.of()))),
            null);
        testBasics(TOP_0.withDeep(TOP_0.deep.withDeepers(Map.of("waht", Set.of()))),
            TOP_0.withDeep(TOP_0.deep.withDeepers(Map.of("what", Set.of()))),
            "/deep/deepers/ unfullfilled<.what.> unexpected<.waht.>");

        testBasics(TOP_0.withDeep(TOP_0.deep.withDeepers(Map.of("what",
                Set.of(Deeper.builder().up(true).f(1.3f).build(), Deeper.builder().up(false).f(0.1f).build())))),
            TOP_0.withDeep(TOP_0.deep.withDeepers(Map.of("what", Set.of()))),
            "/deep/deepers/ \\{what\\} \\* difference in size : expected <0> actual <2>.*");
    }

    @Test
    public void inDebug() {
    }

    private void postponed() {
    }
}
