/**
 * Copyright (C) 2023 Christopher J. Stehno
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cjstehno.ersatz;

import lombok.val;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;

public interface TestHelpers {

    static InputStream resourceStream(final String path) {
        return TestHelpers.class.getResourceAsStream(path);
    }

    static String resourceString(final String path) throws IOException {
        return resourceString(path, null);
    }

    static String resourceString(final String path, final Map<String, Object> replacements) throws IOException {
        var string = IOUtils.toString(resourceStream(path));

        if (replacements != null) {
            for (val ent : replacements.entrySet()) {
                string = string.replaceAll(ent.getKey(), ent.getValue().toString());
            }
        }

        return string;
    }

    static URL resourceUrl(final String path) {
        return TestHelpers.class.getResource(path);
    }

    static URI resourceUri(final String path) throws URISyntaxException {
        return resourceUrl(path).toURI();
    }

    static Path resourcePath(final String path) throws URISyntaxException {
        return Path.of(resourceUri(path));
    }

    static File resourceFile(final String path) throws URISyntaxException {
        return new File(resourceUri(path));
    }
}
