/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import net.mtrop.doom.WadFile;
import net.mtrop.doom.tools.struct.util.IOUtils;
import net.mtrop.doom.texture.Animated;
import net.mtrop.doom.texture.Switches;
import net.mtrop.doom.tools.common.ParseException;
import net.mtrop.doom.tools.common.Utility;

/**
 * Main class for JSwantbls.
 * @author Matthew Tropiano
 */
public final class WSwAnTablesMain
{
	private static final int ERROR_NONE = 0;
	private static final int ERROR_BAD_INPUTOUTPUT_FILE = 1;
	private static final int ERROR_BAD_PARSE = 2;
	private static final int ERROR_MISSING_DATA = 3;
	private static final int ERROR_UNKNOWN = -1;

	public static final String SWITCH_HELP1 = "--help";
	public static final String SWITCH_HELP2 = "-h";
	public static final String SWITCH_VERBOSE1 = "--verbose";
	public static final String SWITCH_VERBOSE2 = "-v";
	public static final String SWITCH_VERSION = "--version";
	public static final String SWITCH_EXPORT1 = "--export";
	public static final String SWITCH_EXPORT2 = "-x";
	public static final String SWITCH_IMPORT1 = "--import";
	public static final String SWITCH_IMPORT2 = "-i";
	public static final String SWITCH_ADDSOURCE1 = "--import-source";
	public static final String SWITCH_ADDSOURCE2 = "-s";

	public static final String SWANTBLS_OUTPUT_HEADER = (new StringBuilder())
		.append("# Table file generated by WSWANTBL v").append(Version.WSWANTBL).append(" by Matt Tropiano")
	.toString();

	/**
	 * Program options.
	 */
	public static class Options
	{
		private PrintStream stdout;
		private PrintStream stderr;
		
		private boolean help;
		private boolean version;
		private boolean verbose;
		private Boolean exportMode;
		private boolean importSource;
		private File sourceFile;
		private File wadFile;
		
		private Options()
		{
			this.stdout = null;
			this.stderr = null;
			this.help = false;
			this.version = false;
			this.verbose = false;
			this.exportMode = null;
			this.importSource = false;
			this.sourceFile = null;
			this.wadFile = null;
		}
		
		public Options setStdout(OutputStream out) 
		{
			this.stdout = new PrintStream(out, true);;
			return this;
		}
		
		public Options setStderr(OutputStream err) 
		{
			this.stderr = new PrintStream(err, true);
			return this;
		}

		public Options setVerbose(boolean verbose)
		{
			this.verbose = verbose;
			return this;
		}
		
		public Options setExportMode(Boolean exportMode)
		{
			this.exportMode = exportMode;
			return this;
		}
		
		public void setImportSource(boolean importSource) 
		{
			this.importSource = importSource;
		}
		
		public Options setSourceFile(File sourceFile)
		{
			this.sourceFile = sourceFile;
			return this;
		}
		
		public Options setWadFile(File wadFile)
		{
			this.wadFile = wadFile;
			return this;
		}
		
	}
	
	/**
	 * Utility context.
	 */
	private static class Context implements Callable<Integer>
	{
		private Options options;
		
		private Context(Options options)
		{
			this.options = options;
		}

		@Override
		public Integer call()
		{
			if (options.help)
			{
				splash(options.stdout);
				usage(options.stdout);
				options.stdout.println();
				help(options.stdout);
				options.stdout.println();
				return ERROR_NONE;
			}
			
			if (options.version)
			{
				splash(options.stdout);
				return ERROR_NONE;
			}
			
			if (options.wadFile == null)
			{
				options.stderr.println("ERROR: No WAD file specified.");
				usage(options.stdout);
				return ERROR_MISSING_DATA;
			}
		
			if (options.exportMode == null)
			{
				options.stderr.println("ERROR: Import or export mode not specified.");
				usage(options.stdout);
				return ERROR_MISSING_DATA;
			}
		
			if (options.sourceFile == null)
			{
				options.stderr.println("ERROR: No source file specified.");
				usage(options.stdout);
				return ERROR_MISSING_DATA;
			}
		
			WadFile wad = null;
			try 
			{
				if (!options.exportMode && !options.wadFile.exists())
					wad = WadFile.createWadFile(options.wadFile);
				else
					wad = new WadFile(options.wadFile);
			}
			catch (FileNotFoundException e)
			{
				options.stderr.printf("ERROR: File %s not found.\n", options.wadFile.getPath());
				return ERROR_BAD_INPUTOUTPUT_FILE;
			}
			catch (IOException e)
			{
				options.stderr.printf("ERROR: %s.\n", e.getLocalizedMessage());
				return ERROR_BAD_INPUTOUTPUT_FILE;
			}
			catch (SecurityException e)
			{
				options.stderr.printf("ERROR: File %s not readable (access denied).\n", options.wadFile.getPath());
				return ERROR_BAD_INPUTOUTPUT_FILE;
			}
		
			String streamName = null;
			BufferedReader reader = null;
			PrintWriter writer = null;
		
			try
			{
				Animated animated;
				boolean replaceAnimated = true;
				if ((animated = wad.getDataAs("ANIMATED", Animated.class)) == null)
				{
					animated = new Animated();
					replaceAnimated = false;
				}
				
				Switches switches;
				boolean replaceSwitches = true;
				if ((switches = wad.getDataAs("SWITCHES", Switches.class)) == null)
				{
					switches = new Switches();
					replaceSwitches = false;
				}
		
				if (options.exportMode)
				{
					try
					{
						writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(options.sourceFile), Charset.forName("ASCII")), true);
						streamName = options.sourceFile.getPath();
					}
					catch (IOException e)
					{
						options.stderr.printf("ERROR: File %s not writable.\n", options.sourceFile.getPath());
						return ERROR_BAD_INPUTOUTPUT_FILE;
					}
					catch (SecurityException e)
					{
						options.stderr.printf("ERROR: File %s not writable (access denied).\n", options.sourceFile.getPath());
						return ERROR_BAD_INPUTOUTPUT_FILE;
					}
		
					Utility.writeSwitchAnimatedTables(switches, animated, SWANTBLS_OUTPUT_HEADER, writer);
					options.stdout.printf("Wrote `%s`.\n", streamName);
				}
				else // import mode
				{
					byte[] sourceData;

					try
					{
						sourceData = IOUtils.getBinaryContents(options.sourceFile);
						reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(sourceData), StandardCharsets.US_ASCII));
						streamName = options.sourceFile.getPath();
					}
					catch (FileNotFoundException e)
					{
						options.stderr.printf("ERROR: File %s not found.\n", options.sourceFile.getPath());
						return ERROR_BAD_INPUTOUTPUT_FILE;
					}
					catch (SecurityException e)
					{
						options.stderr.printf("ERROR: File %s not readable (access denied).\n", options.sourceFile.getPath());
						return ERROR_BAD_INPUTOUTPUT_FILE;
					}
		
					Utility.readSwitchAnimatedTables(reader, animated, switches);
		
					if (replaceAnimated)
					{
						wad.replaceEntry(wad.indexOf("ANIMATED"), animated);
						if (options.verbose)
							options.stdout.printf("Replaced `ANIMATED` in `%s`.\n", options.wadFile.getPath());
					}
					else
					{
						wad.addData("ANIMATED", animated);
						if (options.verbose)
							options.stdout.printf("Added `ANIMATED` to `%s`.\n", options.wadFile.getPath());
					}
					
					if (replaceSwitches)
					{
						wad.replaceEntry(wad.indexOf("SWITCHES"), switches);
						if (options.verbose)
							options.stdout.printf("Replaced `SWITCHES` in `%s`.\n", options.wadFile.getPath());
					}
					else
					{
						wad.addData("SWITCHES", switches);
						if (options.verbose)
							options.stdout.printf("Added `SWITCHES` to `%s`.\n", options.wadFile.getPath());
					}
					
					if (options.importSource)
					{
						int defIndex = wad.indexOf("DEFSWANI");
						if (defIndex >= 0)
						{
							wad.replaceEntry(defIndex, sourceData);
							if (options.verbose)
								options.stdout.printf("Replaced `DEFSWANI` in `%s`.\n", options.wadFile.getPath());
						}
						else
						{
							wad.addData("DEFSWANI", sourceData);
							if (options.verbose)
								options.stdout.printf("Added `DEFSWANI` to `%s`.\n", options.wadFile.getPath());
						}
					}
					
					options.stdout.printf("Imported into `%s`.\n", options.wadFile.getPath());
				}
			}
			catch (IOException e)
			{
				options.stderr.printf("ERROR: %s\n", e.getLocalizedMessage());
				return ERROR_BAD_INPUTOUTPUT_FILE;
			}
			catch (ParseException e)
			{
				options.stderr.printf("ERROR: %s, %s\n", streamName, e.getLocalizedMessage());
				return ERROR_BAD_PARSE;
			}
			finally
			{
				IOUtils.close(reader);
				IOUtils.close(writer);
				IOUtils.close(wad);
			}
			
			return ERROR_NONE;
		}
	}
	
	/**
	 * Reads command line arguments and sets options.
	 * @param out the standard output print stream.
	 * @param err the standard error print stream. 
	 * @param args the argument args.
	 * @return the parsed options.
	 */
	public static Options options(PrintStream out, PrintStream err, String ... args)
	{
		Options options = new Options();
		options.stdout = out;
		options.stderr = err;

		final int STATE_START = 0;
		final int STATE_IMPORTEXPORT = 1;
		int state = STATE_START;
		
		int i = 0;
		while (i < args.length)
		{
			String arg = args[i];
			switch (state)
			{
				case STATE_START:
				{
					if (arg.equals(SWITCH_HELP1) || arg.equals(SWITCH_HELP2))
						options.help = true;
					else if (arg.equals(SWITCH_VERBOSE1) || arg.equals(SWITCH_VERBOSE2))
						options.verbose = true;
					else if (arg.equals(SWITCH_VERSION))
						options.version = true;
					else if (arg.equals(SWITCH_ADDSOURCE1) || arg.equals(SWITCH_ADDSOURCE2))
						options.importSource = true;
					else if (arg.equals(SWITCH_EXPORT1) || arg.equals(SWITCH_EXPORT2))
					{
						state = STATE_IMPORTEXPORT;
						options.exportMode = true;
					}
					else if (arg.equals(SWITCH_IMPORT1) || arg.equals(SWITCH_IMPORT2))
					{
						state = STATE_IMPORTEXPORT;
						options.exportMode = false;
					}
					else
						options.wadFile = new File(arg);
				}
				break;

				case STATE_IMPORTEXPORT:
				{
					options.sourceFile = new File(arg);
					state = STATE_START;
				}
				break;
			}
			i++;
		}
		return options;
	}
	
	/**
	 * Calls the utility using a set of options.
	 * @param options the options to call with.
	 * @return the error code.
	 */
	public static int call(Options options)
	{
		try {
			return (int)(asCallable(options).call());
		} catch (Exception e) {
			e.printStackTrace(options.stderr);
			return ERROR_UNKNOWN;
		}
	}
	
	/**
	 * Creates a {@link Callable} for this utility.
	 * @param options the options to use.
	 * @return a Callable that returns the process error.
	 */
	public static Callable<Integer> asCallable(Options options)
	{
		return new Context(options);
	}
	
	public static void main(String[] args)
	{
		if (args.length == 0)
		{
			splash(System.out);
			usage(System.out);
			System.exit(-1);
			return;
		}

		System.exit(call(options(System.out, System.err, args)));
	}

	/**
	 * Prints the splash.
	 * @param out the print stream to print to.
	 */
	private static void splash(PrintStream out)
	{
		out.println("WSwAnTbl v" + Version.WSWANTBL + " by Matt Tropiano (using DoomStruct v" + Version.DOOMSTRUCT + ")");
	}

	/**
	 * Prints the usage.
	 * @param out the print stream to print to.
	 */
	private static void usage(PrintStream out)
	{
		out.println("Usage: wswantbl [--help | -h | --version] [file] [mode] [switches]");
	}

	/**
	 * Prints the help.
	 * @param out the print stream to print to.
	 */
	private static void help(PrintStream out)
	{
		out.println("    --help              Prints help and exits.");
		out.println("    -h");
		out.println();
		out.println("    --version           Prints version, and exits.");
		out.println();
		out.println("[file]:");
		out.println("    <filename>          The WAD file.");
		out.println();
		out.println("[mode]:");
	    out.println("    --export [dstfile]  Export mode.");
	    out.println("    -x [dstfile]        Exports ANIMATED and SWITCHES from [file] to [dstfile].");
		out.println();
	    out.println("    --import [srcfile]  Import mode.");
	    out.println("    -i [srcfile]        Imports ANIMATED and SWITCHES from [srcfile] into");
	    out.println("                        [file]. WAD file is created if it doesn't exist.");
		out.println();
	    out.println("    --import-source     If Import Mode is active, the source is imported");
	    out.println("    -s                  as \"DEFSWANI\" in the WAD file, as well.");
		out.println();
		out.println("[switches]:");
		out.println("    --verbose           Prints verbose output.");
		out.println("    -v");
	}

}
