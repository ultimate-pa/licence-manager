package de.uni_freiburg.informatik.ultimate.licence_manager;

import java.io.File;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * @author dietsch@informatik.uni-freiburg.de
 */
public class Main {

	public static void main(String[] args) {
		final CommandLine cmds = parseArguments(args);
		if (cmds == null) {
			System.exit(1);
		}
		String directory = cmds.getOptionValue("d");
		File f = new File(directory);
		System.out.println(f.getAbsolutePath());
	}

	private static CommandLine parseArguments(String[] args) {
		final Options cliOptions = createOptions();
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
			return null;
		}
	}

	private static Options createOptions() {
		final Options cliOptions = new Options();
		final Option directory = Option.builder("d").argName("dir").required()
				.hasArg().desc("specify the directory that should be parsed")
				.longOpt("directory").build();

		cliOptions.addOption(directory);
		return cliOptions;
	}
}
