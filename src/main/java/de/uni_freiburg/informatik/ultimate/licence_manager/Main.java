package de.uni_freiburg.informatik.ultimate.licence_manager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * @author dietsch@informatik.uni-freiburg.de
 */
public class Main {

	public static void main(final String[] args) {
		final Options cliOptions = createOptions();
		final CommandLine cmds = parseArguments(args, cliOptions);
		if (cmds == null) {
			System.exit(1);
			return;
		}

		if (cmds.hasOption("h")) {
			printHelp(cliOptions);
			System.exit(0);
			return;
		}

		if (!cmds.hasOption("d")) {
			System.err.print("Missing parameter \"directory\"");
			printHelp(cliOptions);
			System.exit(1);
			return;
		}

		if (!cmds.hasOption("n")) {
			System.err.print("Missing parameter \"name\"");
			printHelp(cliOptions);
			System.exit(1);
			return;
		}

		final String[] fileendings = new String[] { ".java", "pom.xml" };
		final LicenceManager licenceManager = new LicenceManager(
				cmds.getOptionValue("d"), fileendings, cmds.getOptionValue("n"));
		licenceManager.delete();
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
			System.err.println(e.getMessage());
			printHelp(cliOptions);
			return null;
		}
	}

	private static Options createOptions() {
		final Options cliOptions = new Options();

		cliOptions.addOption(Option.builder("d").longOpt("directory")
				.desc("specify the directory that should be parsed")
				.argName("dir").hasArg().build());

		cliOptions.addOption(Option.builder("n").longOpt("name")
				.desc("specify the directory that should be parsed")
				.argName("dir").hasArg().build());

		cliOptions.addOption(Option.builder("del").longOpt("delete")
				.desc("removes all licence notices from the specified files")
				.build());

		cliOptions.addOption("h", "help", false, "prints this message");

		return cliOptions;
	}

	private static void printHelp(final Options cliOptions) {
		final HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("licence-manager [OPTION]", "", cliOptions, "");
	}
}
