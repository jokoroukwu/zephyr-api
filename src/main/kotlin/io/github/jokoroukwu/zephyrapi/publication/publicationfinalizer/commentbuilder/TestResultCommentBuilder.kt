package io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer.commentbuilder;

interface TestResultCommentBuilder {
    fun appendResultComment(comment: Comment)
    override fun toString(): String
}
