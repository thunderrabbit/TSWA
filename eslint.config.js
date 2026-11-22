import js from "@eslint/js";
import globals from "globals";
import html from "eslint-plugin-html";

// ESLint for the JavaScript inside the app's single-file web UI.
// eslint-plugin-html extracts the <script> blocks from index.html;
// the rules are ESLint's recommended set with browser globals.
export default [
  {
    files: ["**/*.html"],
    plugins: { html },
    languageOptions: { globals: globals.browser },
    rules: { ...js.configs.recommended.rules },
  },
];
