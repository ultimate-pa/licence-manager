package de.uni_freiburg.informatik.ultimate.licence_manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 
 * @author dietsch@informatik.uni-freiburg.de
 * 
 */
public class LicenceTemplate {

	private static final String KEYWORD_DATERANGE = "@\\{daterange\\}";
	private final Path mTemplateFile;
	private final String mCurrentYear;
	private final Collection<String> mTemplate;
	private final Pattern mFirstLinePattern;

	public LicenceTemplate(File template) throws IOException {
		mTemplateFile = template.toPath();
		mTemplate = Files.lines(mTemplateFile).sequential()
				.collect(Collectors.toList());
		mCurrentYear = getCurrentYear();
		mFirstLinePattern = getPattern(mTemplate);
	}

	public Stream<String> getWritableTemplate() {
		return getWritableTemplate(getFreshStream(), null);
	}

	public Stream<String> getWritableTemplate(Collection<String> authors)
			throws IOException {
		return getWritableTemplate(getFreshStream(), authors);
	}

	public Stream<String> getWritableTemplate(Stream<String> template,
			Collection<String> authors) {
		if (authors == null || authors.isEmpty()) {
			final Stream<String> newTemplate = removeAuthorLines(template);
			return setDateRange(newTemplate);
		}

		return null;
	}

	/**
	 * @return true if <code>line</code> is a valid instantiation of the first
	 *         line of this template
	 * @throws IOException
	 */
	public boolean isFirstLine(final String line) {
		return mFirstLinePattern.matcher(line).matches();
	}

	private Pattern getPattern(Collection<String> template) {
		final Optional<String> first = template.stream().findFirst();
		if (!first.isPresent()) {
			return null;
		}
		final String[] origParts = first.get().split(KEYWORD_DATERANGE);
		final String regex = Pattern.quote(origParts[0]) + "\\d{4}( - \\d{4})?"
				+ origParts[1];
		return Pattern.compile(regex);
	}

	private Stream<String> setDateRange(Stream<String> template) {
		final String currentYearRegexp = "$1-" + mCurrentYear;
		return template.map(line -> line.replaceAll(KEYWORD_DATERANGE,
				mCurrentYear).replaceAll("(\\d\\d\\d\\d)-\\d\\d\\d\\d",
				currentYearRegexp));
	}

	private Stream<String> removeAuthorLines(Stream<String> template) {
		final Pattern pattern = Pattern.compile("@\\{author:r\\}");
		return template.filter(line -> !pattern.matcher(line).matches());
	}

	private String getCurrentYear() {
		return new SimpleDateFormat("yyyy").format(Calendar.getInstance()
				.getTime());
	}

	private Stream<String> getFreshStream() {
		return mTemplate.stream().sequential();
	}
}
