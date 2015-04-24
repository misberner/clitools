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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import com.misberner.clitools.api.CLITool;

public class CLIToolDispatcher {
	
	private static final Logger LOG = Logger.getLogger(CLIToolDispatcher.class.getName());
	
	private final List<CLIToolFilter> filters = new ArrayList<CLIToolFilter>();
	private Map<String,CLITool> tools;
	private ClassLoader classLoader = ClassLoader.getSystemClassLoader();
	
	private Map<String,CLITool> tools() {
		if (tools == null) {
			updateTools();
		}
		return tools;
	}
	
	public void updateTools(ClassLoader loader) {
		this.classLoader = loader;
		updateTools();
	}
	
	public boolean run(String[] args) throws Exception {
		String toolName = System.getProperty("cli.tool");
		if (toolName == null) {
			System.err.println("Error: I don't know which tool to run.");
			System.err.println("Please re-run with the 'cli.tool' property set to a valid tool name");
			System.err.println("This is a list of all available tools:");
			showTools(System.err);
			return false;
		}
		return runTool(toolName, args);
	}
	
	public boolean runTool(String name, String[] args) throws Exception {
		CLITool tool = tools().get(name);
		if (tool == null) {
			System.err.println("Error: I don't know how to run tool '" + name + "'.");
			System.err.println("This is a list of all available tools:");
			showTools(System.err);
			return false;
		}
		return tool.runMain(args);
	}
	
	public void showTools(PrintStream out) {
		for (CLITool tool : tools().values()) {
			String name = tool.getName();
			out.printf("%10s", name);
			if (name.length() > 10) {
				out.println();
				out.print("    ");
			}
			else {
				out.print("  ");
			}
			out.println(tool.getDescription());
		}
	}
	
	public void updateTools() {
		tools = new HashMap<String,CLITool>();
		for (CLITool tool : ServiceLoader.load(CLITool.class, classLoader)) {
			if (filter(tool)) {
				String name = tool.getName();
				CLITool exTool = tools.put(name, tool);
				if (exTool != null) {
					LOG.warning("Name clash: Tools " + exTool.getClass().getName() + " and "
							+ tool.getClass().getName() + " have the same name '" + name + "'; the former "
							+ "is hidden by the latter");
				}
			}
		}
	}
	
	private boolean filter(CLITool tool) {
		boolean result = true;
		for (CLIToolFilter filter : filters) {
			FilterAction action = filter.apply(tool);
			switch (action) {
			case INCLUDE:
				result = true;
				break;
			case EXCLUDE:
				result = false;
				break;
			default: // case NOOP:
			}
		}
		return result;
	}
	
	protected void addFilter(CLIToolFilter rule, boolean inclusive) {
		if (filters.isEmpty()) {
			filters.add(new UniversalFilter(inclusive ? FilterAction.EXCLUDE : FilterAction.INCLUDE));
		}
		filters.add(rule);
		tools = null;
	}
	
	public void addNameRegexInclude(String nameRegex) {
		addNameRegexFilter(true, nameRegex);
	}
	public void addNameRegexExclude(String nameRegex) {
		addNameRegexFilter(false, nameRegex);
	}
	public void addNameRegexFilter(boolean include, String nameRegex) {
		addRegexFilter(include, nameRegex, null);
	}
	
	public void addClassRegexInclude(String classRegex) {
		addClassRegexFilter(true, classRegex);
	}
	public void addClassRegexExclude(String classRegex) {
		addClassRegexFilter(false, classRegex);
	}
	public void addClassRegexFilter(boolean include, String classRegex) {
		addRegexFilter(include, null, classRegex);
	}
	
	public void addRegexInclude(String nameRegex, String classRegex) {
		addRegexFilter(true, nameRegex, classRegex);
	}
	
	public void addRegexExclude(String nameRegex, String classRegex) {
		addRegexFilter(false, nameRegex, classRegex);
	}
	
	public void addRegexFilter(boolean include, String nameRegex, String classRegex) {
		addFilter(new RegexFilter(nameRegex, classRegex, include), include);
	}
	
	
	
	public static void main(String[] args) throws Exception {
		CLIToolDispatcher dispatcher = new CLIToolDispatcher();
		dispatcher.run(args);
	}
}
