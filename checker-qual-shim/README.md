This is here purely to workaround https://github.com/typetools/checker-framework/issues/6420

We keep a compiletime-only copy of `DefaultQualifier`, with the only change being its @Retention, so that
usages of `DefaultQualifier` within util-core are kept in the compiled jar.
