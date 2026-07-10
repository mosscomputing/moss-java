package com.mosscomputing.moss;

/**
 * MOSS Uninstall Helper for moss-java (checklist-only).
 *
 * <p>Usage: {@code java com.mosscomputing.moss.Uninstall [--dry-run]}
 *
 * <p>Prints a manual-cleanup checklist and exits 0. Performs no filesystem
 * mutation and makes no network calls.
 */
public final class Uninstall {

    private Uninstall() {
    }

    public static void main(String[] args) {
        boolean dryRun = false;
        for (String arg : args) {
            if ("--dry-run".equals(arg)) {
                dryRun = true;
            }
        }

        System.out.println("MOSS Uninstall Helper for moss-java");
        System.out.println("----------------------------------------");
        if (dryRun) {
            System.out.println("[DRY-RUN MODE]");
        }

        System.out.print(
            "\n"
            + "MANUAL CLEANUP CHECKLIST\n"
            + "\n"
            + "[ ] Revoke/rotate MOSS credentials in the MOSS console (API keys / agent capability tokens)\n"
            + "[ ] Remove the moss-sdk dependency (com.mosscomputing:moss-sdk) from your pom.xml / build.gradle\n"
            + "[ ] Remove imports of com.mosscomputing.moss from your .java files\n"
            + "[ ] Remove config files: rm -f .moss.yml moss_config.json moss.config.js\n"
            + "[ ] Unset MOSS_* environment variables\n"
            + "[ ] CI/CD: remove MOSS_* secrets and setup steps from GitHub Actions / CI\n"
            + "[ ] Docker: remove MOSS_* ENV lines and the MOSS dependency from Dockerfiles\n"
            + "[ ] Docs: update README / setup guides that reference MOSS\n");

        System.out.println("\nChecklist printed. Complete the steps above manually.");
        System.exit(0);
    }
}
