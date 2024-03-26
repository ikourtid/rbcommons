package com.rb.nonbiz.text;

/**
 * Some objects take a huge amount of space to write.
 *
 * <p> We usually write objects on a single line in the log. The upside is that it is easier to grep the logs
 * by date. The downside is that most tools (diff, vi, etc.) don't play well with long horizontal lines.
 * Plus, large horizontal lines are hard for a human to read.
 * Therefore, it is sometimes convenient to print over multiple lines. A linear program is a great example. </p>
 *
 * <p> This interface makes the 'can print a multi-line version of toString()' official. </p>
 */
public interface PrintsMultilineString {

  String toMultilineString();

}
