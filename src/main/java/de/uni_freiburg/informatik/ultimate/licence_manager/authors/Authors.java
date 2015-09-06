package de.uni_freiburg.informatik.ultimate.licence_manager.authors;

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

	private Authors() {
		try {
			mProviders = createAuthorProviders();
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
		Map<String, List<Author>> map = authors.stream().collect(
				Collectors.toMap(a -> a.Name,
						a -> Collections.singletonList(a),
						(a1, a2) -> Stream.concat(a1.stream(), a2.stream())
								.collect(Collectors.toList())));

		return map.entrySet().stream()
				.map(entry -> mergeAuthorInstances(entry.getValue()))
				.sorted((a, b) -> a.Name.compareTo(b.Name))
				.collect(Collectors.toList());
	}

	private Author mergeAuthorInstances(final List<Author> authors) {
		final Author rtr = new Author(null, null, null);

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

	private List<IAuthorProvider> createAuthorProviders()
			throws InstantiationException, IllegalAccessException {
		final List<IAuthorProvider> instances = new ArrayList<IAuthorProvider>();
		final Reflections reflections = new Reflections(
				"de.uni_freiburg.informatik.ultimate.licence_manager.authors");

		for (Class<? extends IAuthorProvider> clazz : reflections
				.getSubTypesOf(IAuthorProvider.class)) {
			instances.add(clazz.newInstance());
		}
		return instances;
	}

}
