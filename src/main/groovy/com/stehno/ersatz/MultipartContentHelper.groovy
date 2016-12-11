/*
 * Copyright (C) 2016 Christopher J. Stehno
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
package com.stehno.ersatz

import groovy.transform.CompileStatic
import org.apache.commons.fileupload.FileItem

/**
 * Aids in creating conditions for Multipart request content.
 */
@CompileStatic
class MultipartContentHelper {

    /**
     * Used to determine whether or not the given <code>FileItem</code> has the specified attributes. The allowed attributes are: 'fieldName' to
     * match the file field name, 'string' to match the item value as a string, and 'size' to match the item size value.
     *
     * @param attrs the map of attributes to be matched
     * @param fileItem the multipart file item
     * @return true if the specified conditions are matched
     */
    static boolean eq(final Map<String, Object> attrs, final FileItem fileItem) {
        attrs.every { k, v ->
            if (k == 'fieldName') {
                return v == fileItem.fieldName
            } else if (k == 'string') {
                return v == fileItem.getString('UTF-8')
            } else if (k == 'size') {
                return v == fileItem.size
            } else {
                return false
            }
        }
    }
}
