/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta.options;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class OptionParser<E> {

	private final E options;
	private final Map<String, Option> nameToOption;
	private final Map<String, Field> nameToField;
	private final Set<String> required;

	public OptionParser(E options) {
		this.options = options;
		this.nameToOption = new HashMap<String, Option>();
		this.nameToField = new HashMap<String, Field>();
		this.required = new HashSet<String>();
		for (Field field : options.getClass().getDeclaredFields()) {
			Option option = field.getAnnotation(Option.class);
			if (option != null) {
				String name = option.name();
				nameToOption.put(name, option);
				nameToField.put(name, field);
				if (option.required()) {
					required.add(name);
				}
			}
		}
	}

	public E parse(String[] args) throws OptionException {
		Set<String> todo = new HashSet<String>(required);
		for (int i = 0; i < args.length; i++) {
			Option name = nameToOption.get(args[i]);
			if (name == null) {
				throw new OptionException(String.format("option %s not recognized", args[i]));
			} else {
				todo.remove(args[i]);
				Field field = nameToField.get(args[i]);
				try {
					if (field.getType() == boolean.class) {
						field.setBoolean(options, true);
					} else {
						if (i + 1 >= args.length) {
							throw new OptionException(String.format("option %s requires an argument", name));
						} else {
							String arg = args[i + 1];
							if (field.getType() == int.class) {
								try {
									field.setInt(options, Integer.parseInt(arg));
								} catch (NumberFormatException e) {
									throw new OptionException(String.format("option %s requires an integer argument", name));
								}
							}
							if (field.getType() == String.class) {
								field.set(options, arg);
							}
							i++;
						}
					}
				} catch (IllegalAccessException e) {
					throw new OptionException("could not parse the options");
				}
			}
		}
		if (!todo.isEmpty()) {
			String name = todo.iterator().next();
			throw new OptionException(String.format("option %s is required", name));
		}
		return options;
	}

	public void usage() {
		System.err.println("Options:");
		System.err.println();
		for (Field field : options.getClass().getDeclaredFields()) {
			Option option = field.getAnnotation(Option.class);
			if (option != null) {
				String lhs = option.name();
				if (!option.argument().isEmpty()) {
					lhs = lhs + " " + option.argument();
				}
				System.err.format("    %-16s%s", lhs, option.usage());
				if (option.required()) {
					System.err.print(" [required]");
				}
				System.err.println();
			}
		}
	}
}
