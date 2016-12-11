package com.stehno.ersatz

import groovy.transform.TupleConstructor
import org.apache.commons.fileupload.FileItem

import java.util.function.Function

/**
 * Content matcher (condition) used to assist in matching multipart request content.
 */
class MultipartContentMatcher implements Function<ClientRequest, Boolean> {

    private Closure<Boolean> closure
    private Function<FileItemMatcher, Boolean> function

    static MultipartContentMatcher multipart(@DelegatesTo(FileItemMatcher) final Closure<Boolean> closure) {
        new MultipartContentMatcher(closure: closure)
    }

    static MultipartContentMatcher multipart(final Function<FileItemMatcher, Boolean> function) {
        new MultipartContentMatcher(function: function)
    }

    @Override
    Boolean apply(final ClientRequest clientRequest) {
        FileItemMatcher fileItemMatcher = new FileItemMatcher(clientRequest)
        boolean headerMatch = clientRequest.headers.get(ContentType.CONTENT_TYPE_HEADER, 0).startsWith(ContentType.MULTIPART_MIXED.value)

        if (closure) {
            closure.delegate = fileItemMatcher
            return headerMatch && closure.call(clientRequest)

        } else {
            return headerMatch && function.apply(fileItemMatcher)
        }
    }

    /**
     * Used to determine whether or not the given <code>FileItem</code> has the specified attributes. The allowed attributes are: 'fieldName' to
     * match the file field name, 'string' to match the item value as a string, and 'size' to match the item size value.
     *
     * @param attrs the map of attributes to be matched
     * @param fileItem the multipart file item
     * @return true if the specified conditions are matched
     */
    static boolean attrs(final Map<String, Object> attrs, final FileItem fileItem) {
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

    @TupleConstructor
    static class FileItemMatcher {

        ClientRequest clientRequest

        boolean field(final Map<String, Object> map, final int index) {
            attrs(map, clientRequest.fileItems[index])
        }
    }
}

