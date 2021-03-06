/*
 * Copyright (C) 2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE licence-manager.
 * 
 * The ULTIMATE licence-manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE licence-manager is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE licence-manager. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE licence-manager, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE licence-manager grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.licence_manager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import de.uni_freiburg.informatik.ultimate.licence_manager.authors.Authors;

/**
 * 
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 */
public class Main {

	public static final String OPTION_DIRECTORY = "directory";
	public static final String OPTION_TEMPLATE_NAME = "name";
	public static final String OPTION_DRY_RUN = "dry-run";
	public static final String OPTION_HELP = "help";
	public static final String OPTION_DELETE = "delete";
	public static final String OPTION_DISABLE_SVN = "disable-svn";

	private static CommandLine sOptions;

	public static void main(final String[] args) {
		final Options cliOptions = createOptions();
		sOptions = parseArguments(args, cliOptions);
		if (sOptions == null) {
			System.exit(1);
			return;
		}

		if (hasOption(OPTION_HELP)) {
			printHelp(cliOptions);
			System.exit(0);
			return;
		}

		// check required parameters
		final String[] requiredOptions = new String[] { OPTION_DIRECTORY,
				OPTION_TEMPLATE_NAME };
		for (final String requiredOption : requiredOptions) {
			if (!hasOption(requiredOption)) {
				System.err
						.print("Missing parameter \"" + requiredOption + "\"");
				printHelp(cliOptions);
				System.exit(1);
				return;
			}
		}

		try {

			final String[] fileendings = new String[] { ".java", "pom.xml" };
			final LicenceManager licenceManager = new LicenceManager(
					getOptionValue(OPTION_DIRECTORY), fileendings,
					getOptionValue(OPTION_TEMPLATE_NAME));

			if (hasOption(OPTION_DRY_RUN)) {
				System.out.println("This is a dry run:");
				if (hasOption(OPTION_DELETE)) {
					licenceManager.deleteDry();
				} else {
					licenceManager.writeDry();
				}
			} else {
				if (hasOption(OPTION_DELETE)) {
					licenceManager.delete();
				} else {
					licenceManager.write();
				}
			}
			System.out.println();
			System.out.println("Found the following authors:");
			Authors.getCollectedAuthorNames().forEach(System.out::println);
			System.exit(0);
			return;
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
			System.exit(1);
			return;
		}

	}

	public static boolean hasOption(final String optionName) {
		if (sOptions == null) {
			return false;
		}
		return sOptions.hasOption(optionName);
	}

	public static String getOptionValue(final String optionName) {
		if (sOptions == null) {
			return null;
		}
		return sOptions.getOptionValue(optionName);
	}

	private static CommandLine parseArguments(final String[] args,
			final Options cliOptions) {
		final CommandLineParser cliParser = new DefaultParser();
		try {
			final CommandLine cmds = cliParser.parse(cliOptions, args);
			if (!cmds.getArgList().isEmpty()) {
				System.err.print("Superfluous arguments: "
						+ String.join(",", cmds.getArgList()));
				return null;
			}
			return cmds;
		} catch (ParseException e) {
			System.err
					.println("Exception while parsing command line arguments: "
							+ e.getMessage());
			printHelp(cliOptions);
			return null;
		}
	}

	private static Options createOptions() {
		final Options cliOptions = new Options();

		cliOptions.addOption(Option.builder().longOpt(OPTION_DISABLE_SVN)
				.desc("do not try to use SVN blame to get author information")
				.build());

		cliOptions.addOption(Option.builder("d").longOpt(OPTION_DIRECTORY)
				.desc("specify the directory that should be parsed")
				.argName("dir").hasArg().build());

		cliOptions.addOption(Option.builder("n").longOpt(OPTION_TEMPLATE_NAME)
				.desc("specify the licence template file").argName("dir")
				.hasArg().build());

		cliOptions.addOption(Option.builder().longOpt(OPTION_DELETE)
				.desc("delete all licence texts").build());

		cliOptions
				.addOption(Option
						.builder()
						.longOpt(OPTION_DRY_RUN)
						.desc("do not perform any actual change, just print what would happen")
						.build());

		cliOptions.addOption("h", OPTION_HELP, false, "prints this message");

		return cliOptions;
	}

	private static void printHelp(final Options cliOptions) {
		final HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("licence-manager [OPTION]", "", cliOptions, "");
	}
}
