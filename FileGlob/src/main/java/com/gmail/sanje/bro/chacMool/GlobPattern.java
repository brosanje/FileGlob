package com.gmail.sanje.bro.chacMool;

/**
 * File Glob pattern matcher.
 * Does not use regex internally, instead it compiles the pattern into a list of anchors.
 * 
 * It's nice java 8 has java.nio.file.PathMatcher, but what about if you want to be compatible
 * with older versions of java?
 * 
 * Patterns are composed of * and ?.
 * Use a ? to match exactly one arbitrary character.  The regex equivalent is ..
 * Use a * to match any number of characters, including none.  The regex equivalent is .*.
 * Use a \ to escape ? * or backslash itself.  however, a backslash followed by any other
 * character will be exactly that, a backslash and the other character.
 * 
 * @author <a href="mailto:bro.sanje@gmail.com">Bro Sanje</a>
 * @date May 27, 2016
 * @version 1.0
 *
 */
public class GlobPattern {
	
	private static class Anchor {
		boolean fixedOffset = true;
		int offset = 0;
		int positionMatch = -1;
		String chunk = "";
		boolean atBeginning = false;
		boolean atEnd = false;
		
		Anchor resetOffset() { if (!fixedOffset) offset = 0; return this; }
	}
	
	private String _rawPattern = null;
	private boolean _noPattern = false;
	private java.util.ArrayList<Anchor> _pieces = null;
	
	private final static java.util.regex.Pattern _glob = java.util.regex.Pattern.compile("(?:[*?]|\\\\\\\\|\\\\\\*|\\\\\\?)");
	private final static java.util.regex.Pattern _glob2regex = java.util.regex.Pattern.compile("(?:[*?]|\\\\\\\\|\\\\\\*|\\\\\\?|\\\\Q|\\\\E)");
	private final static java.util.regex.Pattern _regexCharacters = java.util.regex.Pattern.compile("[^A-Za-z0-9_=!@#$%;:-]");
	
	private GlobPattern() {
	}

	/**
	 * Convert a file glob pattern into a regex pattern.
	 * Essentially, ? is turned into . and * is turned into .*.
	 * If regex characters are found, they are quoted with \Q and \E.
	 * 
	 * \* and \? quotes the fileglob wildcards, and \\ quotes the backslash.
	 * any other occurrence of backslash is left asis.
	 * for example windows filenames would not require even more backslashes.
	 * 
	 * @param pattern_ the file glob pattern to convert to a java regex pattern.
	 * @return the java regex pattern for pattern_
	 */
	public static java.util.regex.Pattern compileToRegex(String pattern_) {
		return java.util.regex.Pattern.compile(compileToRegexString(pattern_));	
	}
	
	/**
	 * Convert a file glob pattern into a regex string.
	 * Essentially, ? is turned into . and * is turned into .*.
	 * If regex characters are found, they are quoted with \Q and \E.
	 * 
	 * \* and \? quotes the fileglob wildcards, and \\ quotes the backslash.
	 * any other occurrence of backslash is left asis.
	 * for example windows filenames would not require even more backslashes.
	 * 
	 * @param pattern_ the file glob pattern to convert to a java regex string.
	 * @return the java regex string for pattern_
	 */
	public static String compileToRegexString(String pattern_) {
		StringBuilder sb = new StringBuilder();
		int pp = 0;
		java.util.regex.Matcher mm = _glob2regex.matcher(pattern_);
		
		while (pattern_.length() > pp) {
			int _start_ = pattern_.length();
			int _end_ = _start_;

			String _wildcard_ = "";
			if (mm.find()) {
				_start_ = mm.start();
				_end_ = mm.end();
				_wildcard_ = pattern_.substring(_start_, _end_);
			}
			
			if (pp < _start_) {
				String _chunk_ = pattern_.substring(pp,_start_);
				if (_regexCharacters.matcher(_chunk_).find())
					sb.append("\\Q").append(_chunk_).append("\\E");
				else
					sb.append(_chunk_);
			}
			
			if (_wildcard_.equals("*"))
				sb.append(".*");
			else if (_wildcard_.equals("?"))
				sb.append(".");
			else if (_wildcard_.equals("\\Q"))
				sb.append("\\\\Q");
			else if (_wildcard_.equals("\\E"))
				sb.append("\\\\E");
			else if (!_wildcard_.isEmpty())
				sb.append(_wildcard_);
			
			pp = _end_;
		}

		return sb.toString();
	}
	
	/**
	 * Constructor for GlobPattern
	 * 
	 * @param pattern_ a pattern string
	 * @return a new GlobPattern
	 */
	public static GlobPattern compile(String pattern_) {		
		GlobPattern _this_ = new GlobPattern();
		_this_._pieces = new java.util.ArrayList<Anchor>();
		_this_._rawPattern = pattern_;
		java.util.ArrayList<Anchor> pieces = _this_._pieces;
		
		Anchor aa = new Anchor();
		aa.atBeginning = true;
		pieces.add(aa);

		int pp = 0;
		java.util.regex.Matcher mm = _glob.matcher(pattern_);
		while (pattern_.length() >= pp) {
			int _start_ = pattern_.length();
			int _end_ = _start_;

			int _pos_ = pp;
			String _wildcard_ch_ = "";
			String _chunk_ = "";
			boolean _wildcard_found_ = false;
			while (!_wildcard_found_) {
				if (mm.find()) {
					_start_ = mm.start();
					_end_ = mm.end();
					_wildcard_ch_ = pattern_.substring(_start_, _end_);
					if (_wildcard_ch_.equals("\\*") || _wildcard_ch_.equals("\\?") || _wildcard_ch_.equals("\\\\")) {
						_chunk_ += (_pos_ < _start_ ? pattern_.substring(_pos_, _start_) : "") + _wildcard_ch_.charAt(1);
						_pos_ = _end_;
					} else {
						if (_pos_ < pattern_.length())
							_chunk_ += pattern_.substring(_pos_, _start_);
						_wildcard_found_ = true;
						pp = _end_;
					}
				} else {
					if (0 == pp) {
						_this_._noPattern = true;
						if (!_chunk_.isEmpty())
							if (_pos_ < pattern_.length())
								_this_._rawPattern = _chunk_ + pattern_.substring(_pos_);
							else
								_this_._rawPattern = _chunk_;
						return _this_;
					}
					
					if (_pos_ < pattern_.length())
						_chunk_ += pattern_.substring(_pos_);
					pp = _start_ = _end_ = pattern_.length();
					break;
				}
			}
			
			if (!_chunk_.isEmpty()) {
				aa.chunk = _chunk_;
				aa = new Anchor();
				pieces.add(aa);
				if (_wildcard_ch_.equals("?")) {
					aa.fixedOffset = true;
					aa.offset = 1;
				} else if (_wildcard_ch_.equals("*")) {
					aa.fixedOffset = false;
					aa.offset = 0;
				} else if (pattern_.length() <= pp) {
					aa.atEnd = true;
					break;
				}
			} else {
				if (_wildcard_ch_.equals("?")) {
					if (aa.fixedOffset)
						aa.offset++;
					else {
						aa = new Anchor();
						pieces.add(aa);
						aa.fixedOffset = true;
						aa.offset = 1;
					}
				} else if (_wildcard_ch_.equals("*")) {
					if (aa.fixedOffset) {
						if (aa.offset > 0) {
							aa = new Anchor();
							pieces.add(aa);
						}
						aa.fixedOffset = false;
					} else {
						aa.fixedOffset = false;
						aa.offset = 0;
					}
				} else if (pattern_.length() <= pp) {
					aa.atEnd = true;
					break;
				}
			}
		}
		
		return _this_;
	}
	
	public boolean matches(String text_) {
		if (_noPattern) return _rawPattern.equals(text_);
		
		boolean _matches_ = false;
		
		int pp = 0;
		java.util.ListIterator<Anchor> ii = _pieces.listIterator();
		try {
			Anchor aa = ii.next().resetOffset();
			boolean _fail_ = false;
			while (text_.length() >= pp) {
				if (_fail_) {
					aa = ii.previous();	// the first call to previous turns us around but returns the same element
					while (_fail_) {
						if (!ii.hasPrevious())
							break;
						if (!aa.fixedOffset)
							aa.offset = 0;
						
						aa = ii.previous();
						if (!aa.fixedOffset) {
							aa.offset++;
							pp = aa.positionMatch;
							_fail_ = false;
							aa = ii.next(); // turn around
						}
					}
				}
				if (_fail_) break;
				
				if (aa.atBeginning && 0 != pp) {
					_fail_ = true;
					continue;
				}
				
				int pos = 0;
				if (aa.fixedOffset) {
					pos = pp + aa.offset;
					if (pos+aa.chunk.length() <= text_.length()) {
						if (aa.chunk.isEmpty() || text_.regionMatches(pos, aa.chunk, 0, aa.chunk.length())) {
						} else {
							_fail_ = true;
							continue;
						}
					} else {
						_fail_ = true;
						continue;
					}
				} else {
					pos = aa.chunk.isEmpty() ? pp+aa.offset : text_.indexOf(aa.chunk, pp+aa.offset);
					if (-1 == pos) {
						_fail_ = true;
						continue;
					}
					
					if (aa.offset < pos - pp)
						aa.offset = pos-pp;
				}
				
				aa.positionMatch = pp;
				pp = pos + aa.chunk.length();
				
				if (pp >= text_.length()) {
					if (!ii.hasNext() && aa.atEnd) {
						_matches_ = true;
						break;
					}
				} else if (!ii.hasNext()) {
					if (!aa.fixedOffset) {
						if (aa.chunk.isEmpty() || text_.endsWith(aa.chunk)) {
							_matches_ = true;
							break;
						}
					}
				}
				
				if (!ii.hasNext()) {
					_fail_ = true;
					continue;
				}
				
				aa = ii.next().resetOffset();
			}
		} catch (java.util.NoSuchElementException ee) {
			
		}
		
		return _matches_;
	}
}