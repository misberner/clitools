/* 
 * Copyright (c) 2015 Malte Isberner
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.misberner.clitools;

import java.util.regex.Pattern;

import com.misberner.clitools.api.CLITool;

final class RegexFilter implements CLIToolFilter {
	
	private final Pattern namePattern;
	private final Pattern classPattern;
	private final FilterAction matchAction;

	public RegexFilter(String namePattern, String classPattern, boolean include) {
		this.namePattern = (namePattern != null) ? Pattern.compile(namePattern) : null;
		this.classPattern = (classPattern != null) ? Pattern.compile(classPattern) : null;
		this.matchAction = include ? FilterAction.INCLUDE : FilterAction.EXCLUDE;
	}

	public FilterAction apply(CLITool tool) {
		if (namePattern != null) {
			if (namePattern.matcher(tool.getName()).matches()) {
				return matchAction;
			}
		}
		if (classPattern != null) {
			if (classPattern.matcher(tool.getClass().getName()).matches()) {
				return matchAction;
			}
		}
		return FilterAction.NOOP;
	}
}
