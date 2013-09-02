package ext.deployit.community.plugin.restrictplaceholders.util;

import static java.util.regex.Matcher.quoteReplacement;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.reverse;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;
import com.xebialabs.deployit.plugin.api.udm.Dictionary;
import com.xebialabs.deployit.plugin.api.udm.Environment;

public class Dictionaries {
    // "{{...}}"
    private static Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([^\\}]+)\\}\\}");

    public static Map<String, String> consolidatedDictionary(Environment environment) {
        // can't use ImmutableMap.Builder as it does not allow key overrides
        Map<String, String> flattenedDictionary = newHashMap();
        // top-most dictionaries *override* lower dictionaries
        for (Dictionary dictionary : reverse(environment.getDictionaries())) {
            flattenedDictionary.putAll(dictionary.getEntries());
        }
        resolvePlaceholdersInValues(flattenedDictionary);
        return ImmutableMap.copyOf(flattenedDictionary);
    }
    
    // modifies the input map
    private static void resolvePlaceholdersInValues(Map<String, String> entries) {
        // while (resol...) {} would be shorter but this is easier to read
        boolean replacedPlaceholders;
        do {
            replacedPlaceholders = resolveFirstPlaceholdersInValues(entries);
        } while (replacedPlaceholders);
    }
    
    // modifies the input map
    private static boolean resolveFirstPlaceholdersInValues(Map<String, String> entries) {
        boolean foundPlaceholder = false;
        ImmutableMap.Builder<String, String> resolvedEntries = ImmutableMap.builder();
        for (Entry<String, String> entry : entries.entrySet()) {
            String value = entry.getValue();
            Matcher placeholderMatches = PLACEHOLDER_PATTERN.matcher(value);
            if (placeholderMatches.find()) {
                foundPlaceholder = true;
                String key = entry.getKey();
                String placeholder = placeholderMatches.group(1);
                checkState(!placeholder.equals(key), "Dictionary entry '%s' refers to itself", key);
                checkArgument(entries.containsKey(placeholder), 
                        "Dictionary entry '%s' refers to non-existent placeholder '%s'", value, placeholder);
                /*
                 * For convenience, only replace the first placeholder per entry found.
                 * This leads to multiple passes to resolve a value such as 
                 * "{{FOO}} and {{BAR}}", but is much easier than handling all the matching groups
                 */
                resolvedEntries.put(key, placeholderMatches.replaceFirst(quoteReplacement(entries.get(placeholder))));
            }
        }
        entries.putAll(resolvedEntries.build());
        return foundPlaceholder;
    }
}