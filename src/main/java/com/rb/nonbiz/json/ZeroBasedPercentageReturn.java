package com.rb.nonbiz.json;

/**
 * Represents the serialization type of a return, used in the JSON API documentation only.
 *
 * <p> The JSON API represents returns as zero-based, and as percentages.
 * For example, onesBasedReturn(1.02) will be '2' in the JSON API, and onesBasedReturn(0.97) will be '-3'.
 * Therefore, we should not confuse the JSON API documentation with the term 'OnesBasedReturn',
 * which is not an accurate term from the point of view of the JSON API.
 * We will instead use this class, with its own JsonApiDocumentation. </p>
 *
 * <p> This is an intentionally blank class, because it is not used to represent returns in the optimization;
 * it is just a hacky placeholder to aid the JSON API documentation generation. </p>
 */
public class ZeroBasedPercentageReturn {

}
