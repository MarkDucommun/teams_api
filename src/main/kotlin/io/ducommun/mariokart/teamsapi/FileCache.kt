package io.ducommun.mariokart.teamsapi

import reactor.core.publisher.Mono

data class FileCache(
    private var html: Mono<String>? = null,
    private var css: Mono<String>? = null,
    private var js: Mono<String>? = null
) {

    operator fun get(type: FileType): Mono<String>? =
        when (type) {
            FileType.HTML -> html
            FileType.CSS -> css
            FileType.JS -> js
        }

    operator fun set(type: FileType, value: Mono<String>) {

        when (type) {
            FileType.HTML -> html = value
            FileType.CSS -> css = value
            FileType.JS -> js = value
        }
    }
}

enum class FileType {
    HTML, CSS, JS
}