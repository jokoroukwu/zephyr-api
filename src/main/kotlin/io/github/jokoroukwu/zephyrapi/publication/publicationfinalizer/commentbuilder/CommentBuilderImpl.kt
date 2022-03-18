package io.github.jokoroukwu.zephyrapi.publication.publicationfinalizer.commentbuilder

import io.github.jokoroukwu.zephyrapi.publication.testresultstatuscomplementor.TestResultStatus
import java.lang.StringBuilder

private const val LINE_BREAK = "<br>"
private const val BOLD_TAG_OPEN = "<strong>"
private const val BOLD_TAG_CLOSE = "</strong>"
private const val CLOSE_COLOR_TAG = "</span>"
private const val OPEN_RED_COLOR_TAG = """<span style="color: rgb(160, 0, 0);">"""
private const val OPEN_GREEN_COLOR_TAG = """<span style="color: rgb(0, 100, 50);">"""

class  CommentBuilderImpl : TestResultCommentBuilder {

    private val commentList: MutableList<Comment> = ArrayList(5)

    override fun appendResultComment(comment: Comment) {
        commentList.add(comment)
    }

    /**
     * Transforms comments into a bulletin list where each row
     * begins at new line, prefixed by its index.
     *
     * Rows that represent passed results are colored in green,
     * whereas those that represent failed results are in red
     *
     * @return a string representation padded with tags for proper formatting
     */
    override fun toString(): String {
        if (commentList.isEmpty()) {
            return ""
        }
        val builder = StringBuilder((128 * commentList.size) + 128)
        builder.append(BOLD_TAG_OPEN)
            .append("Results:")
            .append(BOLD_TAG_CLOSE)
            .append(LINE_BREAK)
            .append(LINE_BREAK)

        commentList.forEachIndexed { i, comment ->
            builder.append(comment.evaluateColor())
                .append(i + 1)
                .append(") ")
                .append(comment.status)
            if (comment.text.isNotBlank()) {
                builder.append(':')
                    .append(LINE_BREAK)
                    .append(comment.text)
            }
            builder.append(CLOSE_COLOR_TAG)
                .append(LINE_BREAK)
                .append(LINE_BREAK)
        }
        return builder.toString()
    }

    private fun Comment.evaluateColor(): String {
        return if (status == TestResultStatus.PASS) OPEN_GREEN_COLOR_TAG else OPEN_RED_COLOR_TAG
    }

}