package io.github.cjstehno.mtchr;

import lombok.val;
import org.junit.jupiter.api.Test;

import static io.github.cjstehno.mtchr.Mtchrs.not;
import static io.github.cjstehno.mtchr.Mtchrs.startsWith;
import static org.junit.jupiter.api.Assertions.*;

class MtchrsTest {

    @Test void matchers(){
        val matcher = startsWith("/foo/");

        assertTrue(matcher.matches("/foo/bar"));
        assertFalse(matcher.matches("/alpha/foo"));
    }

    @Test void anding(){
        val matcher = Mtchrs.and(
            startsWith("/alpha/"),
            Mtchrs.endsWith(".txt")
        );

        assertTrue(matcher.matches("/alpha/charlie.txt"));
        assertFalse(matcher.matches("/alpha/charlie.jar"));
        assertFalse(matcher.matches("/bravo/charlie.txt"));

        val describer = new StringDescriber();
        matcher.describe(describer);
        val description = describer.describe();
        assertEquals("a string starting with \"/alpha/\" and a string ending with \".txt\"", description);
    }

    @Test void notting(){
        val matcher = not(startsWith("/something/"));

        assertTrue(matcher.matches("/other/else"));
        assertFalse(matcher.matches("/something/else"));

        val describer = new StringDescriber();
        matcher.describe(describer);
        val description = describer.describe();
        assertEquals("not a string starting with \"/something/\"", description);
    }
}

