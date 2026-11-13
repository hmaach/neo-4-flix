package com.neo4flix.common.error;

import java.util.Map;

// The only error shape the API returns. fieldErrors is empty unless validation failed.
public record ApiError(int status, String error, String message, Map<String, String> fieldErrors) {
}
