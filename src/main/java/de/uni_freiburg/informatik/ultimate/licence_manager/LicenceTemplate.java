/*
 * Copyright (C) 2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * Copyright (C) 2015 dietsch@informatik.uni-freiburg.de
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import de.uni_freiburg.informatik.ultimate.licence_manager.authors.Author;
import de.uni_freiburg.informatik.ultimate.licence_manager.util.CachedFileStream;
import de.uni_freiburg.informatik.ultimate.licence_manager.util.DateUtils;

/**
 * 
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 */
public class LicenceTemplate {

	private static final String KEYWORD_DATERANGE = "@\\{daterange\\}";
	private static final String KEYWORD_AUTHOR = "@\\{author:r\\}";
	private final CachedFileStream mTemplate;
	private final Pattern mPatternFirstLine;
	private final Pattern mPatternAuthor;

	public LicenceTemplate(CachedFileStream template) throws IOException {
		mTemplate = template;
		mPatternFirstLine = getPattern(mTemplate.getList());
		mPatternAuthor = Pattern.compile(KEYWORD_AUTHOR);
	}

	public Stream<String> getWritableTemplate() {
		return getWritableTemplate(null);
	}

	public Stream<String> getWritableTemplate(List<Author> authors) {
		if (authors == null) {
			authors = Collections.emptyList();
		}
		return setDateRangeFromKeyword(replaceAuthorLines(
				mTemplate.getStream(), authors));
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
		final String currentYearRegexp = "$1-" + DateUtils.getCurrentYear();
		return template.map(line -> line.replaceAll(KEYWORD_DATERANGE,
				DateUtils.getCurrentYear()).replaceAll(
				"(\\d\\d\\d\\d)-\\d\\d\\d\\d", currentYearRegexp));
	}

	private String replaceDateRangeWithAuthorDates(String line, Author author) {
		if (author.YearFrom != null) {
			if (author.YearFrom.equals(DateUtils.getCurrentYear())) {
				return line.replaceAll(KEYWORD_DATERANGE, author.YearFrom);
			} else {
				return line.replaceAll(KEYWORD_DATERANGE, author.YearFrom + "-"
						+ DateUtils.getCurrentYear());
			}
		}
		return line;
	}

	// private Stream<String> removeAuthorLines(Stream<String> template) {
	// return template.filter(line -> !mPatternAuthor.matcher(line).matches());
	// }

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
}
