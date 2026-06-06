package de.cidaas.sdk.android.cidaasverification.view.pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * Encodes a 9-dot pattern path as {@code PREFIX[d1,d2,...]} with 1-based cell indices (cidaas convention, e.g. {@code RED[1,2,3]}).
 * Pattern enrollment sends {@code SHA-256(UTF-8 bytes of this string)} as lowercase hex in {@code pass_code}.
 */
public final class PatternPasscodeFormatter {

    private PatternPasscodeFormatter() {
    }

    /**
     * @param prefix optional color/prefix segment; when null or blank, {@code RED} is used (matches common cidaas examples)
     * @param cellsZeroBased ordered cell indices 0–8 (top-left row-major)
     */
    @NonNull
    public static String format(@Nullable String prefix, @NonNull List<Integer> cellsZeroBased) {
        String p = (prefix != null && !prefix.trim().isEmpty()) ? prefix.trim() : "RED";
        StringBuilder sb = new StringBuilder(p).append('[');
        for (int i = 0; i < cellsZeroBased.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            int c = cellsZeroBased.get(i);
            sb.append(c + 1);
        }
        return sb.append(']').toString();
    }
}
