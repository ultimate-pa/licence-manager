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
package de.uni_freiburg.informatik.ultimate.licence_manager.authors;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.reflections.Reflections;

import de.uni_freiburg.informatik.ultimate.licence_manager.LicencedFile;
import de.uni_freiburg.informatik.ultimate.licence_manager.filetypes.IFileTypeDependentOperation;

/**
 * 
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 */
public class Authors {

	private final static Authors sInstance = new Authors();

	private final List<IAuthorProvider> mProviders;
	private final List<IAuthorRenamer> mRenamers;

	private Authors() {
		try {
			mProviders = createInstances(IAuthorProvider.class);
			mRenamers = createInstances(IAuthorRenamer.class);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static List<Author> getAuthors(final LicencedFile file,
			final IFileTypeDependentOperation operation) {
		return sInstance.getAuthorsInternal(file, operation);
	}

	private List<Author> getAuthorsInternal(final LicencedFile file,
			final IFileTypeDependentOperation operation) {
		final List<Author> rtr = new ArrayList<Author>();
		for (final IAuthorProvider provider : mProviders) {
			if (provider.isUsable(operation.getFileType())) {
				rtr.addAll(provider.getAuthors(file, operation));
			}
		}
		return mergeAuthors(rtr);
	}

	private List<Author> mergeAuthors(final List<Author> authors) {

		final Map<String, List<Author>> mapWithDuplicates = authors.stream().map(this::rename)
				.collect(
						Collectors.toMap(
								a -> a.Name,
								a -> Collections.singletonList(a),
								(a1, a2) -> Stream.concat(a1.stream(),
										a2.stream()).collect(
										Collectors.toList())));

		return mapWithDuplicates.entrySet().stream()
				.map(entry -> mergeAuthorInstances(entry.getValue()))
				.sorted((a, b) -> a.Name.compareTo(b.Name))
				.collect(Collectors.toList());
	}

	private Author rename(Author author) {
		for (final IAuthorRenamer renamer : mRenamers) {
			if (renamer.shouldRename(author)) {
				author = new Author(renamer.newName(author), author.YearFrom,
						author.YearTo);
			}
		}
		return author;
	}

	private Author mergeAuthorInstances(final List<Author> authors) {
		Author rtr = new Author(null, null, null);

		for (final Author author : authors) {
			rtr.Name = author.Name;
			rtr.YearFrom = min(rtr.YearFrom, author.YearFrom);
			rtr.YearTo = max(rtr.YearTo, author.YearTo);
		}

		return rtr;
	}

	private String min(String a, String b) {
		if (a == null && b == null) {
			return null;
		}
		if (a != null && b == null) {
			return a;
		}
		if (b != null && a == null) {
			return b;
		}
		if (Integer.valueOf(a) < Integer.valueOf(b)) {
			return a;
		}
		return b;
	}

	private String max(String a, String b) {
		if (a == null && b == null) {
			return null;
		}
		if (a != null && b == null) {
			return a;
		}
		if (b != null && a == null) {
			return b;
		}
		if (Integer.valueOf(a) > Integer.valueOf(b)) {
			return a;
		}
		return b;
	}

	private <T> List<T> createInstances(Class<? extends T> clazz)
			throws InstantiationException, IllegalAccessException {
		final List<T> instances = new ArrayList<T>();
		final Reflections reflections = new Reflections(
				"de.uni_freiburg.informatik.ultimate.licence_manager.authors");

		for (Class<? extends T> concreteClazz : reflections
				.getSubTypesOf(clazz)) {
			instances.add(concreteClazz.newInstance());
		}
		return instances;
	}

}
