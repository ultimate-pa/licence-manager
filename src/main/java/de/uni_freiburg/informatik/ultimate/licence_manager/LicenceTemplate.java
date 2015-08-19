package de.uni_freiburg.informatik.ultimate.licence_manager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import de.uni_freiburg.informatik.ultimate.licence_manager.util.CachedFileStream;

/**
 * 
 * @author dietsch@informatik.uni-freiburg.de
 * 
 */
public class LicenceTemplate {

	private static final String KEYWORD_DATERANGE = "@\\{daterange\\}";
	private static final String KEYWORD_AUTHOR = "@\\{author:r\\}";
	private final CachedFileStream mTemplate;
	private final Pattern mPatternFirstLine;
	private final Pattern mPatternAuthor;

	private String mCurrentYear;

	public LicenceTemplate(CachedFileStream template) throws IOException {
		mTemplate = template;
		mCurrentYear = getCurrentYear();
		mPatternFirstLine = getPattern(mTemplate.getList());
		mPatternAuthor = Pattern.compile(KEYWORD_AUTHOR);
	}

	public Stream<String> getWritableTemplate() {
		return getWritableTemplate(null);
	}

	public Stream<String> getWritableTemplate(List<Author> authors) {
		final Author university = getUniversity();
		if (authors == null || authors.isEmpty()) {
			authors = Collections.singletonList(university);
		}
		if (!authors.stream().anyMatch(a -> a.Name.equals(university.Name))) {
			authors.add(university);
		}
		Collections.sort(
				authors,
				(a, b) -> a.Name.equals(university.Name) ? -1 : (b.Name
						.equals(university.Name) ? 1 : a.Name
						.compareTo(b.Name)));
		return setDateRangeFromKeyword(replaceAuthorLines(mTemplate.getStream(),
				authors));
	}

	/**
	 * @return true if <code>line</code> is a valid instantiation of the first
	 *         line of this template
	 * @throws IOException
	 */
	public boolean isFirstLine(final String line) {
		return mPatternFirstLine.matcher(line).matches();
	}

	private Pattern getPattern(Collection<String> template) {
		final Optional<String> first = template.stream().findFirst();
		if (!first.isPresent()) {
			return null;
		}

		final String regex = first.get().replaceAll("\\(", "\\\\(")
				.replaceAll("\\)", "\\\\)")
				.replaceAll(KEYWORD_DATERANGE, "\\\\d{4}(\\s*-\\s*\\\\d{4})?")
				.replaceAll(KEYWORD_AUTHOR, ".*");

		return Pattern.compile(regex);
	}

	private Stream<String> setDateRangeFromKeyword(Stream<String> template) {
		final String currentYearRegexp = "$1-" + mCurrentYear;
		return template.map(line -> line.replaceAll(KEYWORD_DATERANGE,
				mCurrentYear).replaceAll("(\\d\\d\\d\\d)-\\d\\d\\d\\d",
				currentYearRegexp));
	}

	private String replaceDateRangeWithAuthorDates(String line, Author author) {
		if (author.YearFrom != null) {
			if (author.YearFrom.equals(getCurrentYear())) {
				return line.replaceAll(KEYWORD_DATERANGE, author.YearFrom);
			} else {
				return line.replaceAll(KEYWORD_DATERANGE, author.YearFrom + "-"
						+ getCurrentYear());
			}
		}
		return line;
	}

//	private Stream<String> removeAuthorLines(Stream<String> template) {
//		return template.filter(line -> !mPatternAuthor.matcher(line).matches());
//	}

	private Stream<String> replaceAuthorLines(final Stream<String> template,
			final Collection<Author> authors) {
		final ArrayList<String> rtr = new ArrayList<>();
		template.forEach(line -> {
			final Matcher matcher = mPatternAuthor.matcher(line);
			if (!matcher.find()) {
				rtr.add(line);
			} else {
				for (final Author author : authors) {
					rtr.add(replaceDateRangeWithAuthorDates(
							matcher.replaceAll(author.Name), author));
				}
			}
		});
		return rtr.stream();
	}

	private String getCurrentYear() {
		if (mCurrentYear == null) {
			mCurrentYear = new SimpleDateFormat("yyyy").format(Calendar
					.getInstance().getTime());
		}
		return mCurrentYear;
	}

	private Author getUniversity() {
		return new Author("University of Freiburg", getCurrentYear(), null);
	}
}
