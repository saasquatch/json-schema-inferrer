# Changelog

## [Unreleased]

## [0.2.1] - 2024-01-05

### Added

- (Beta) Added support for JSON Path.

### Changed

- Internal dependency version bumps.

## [0.2.0] - 2023-04-24

### Changed

- Switched to Gradle.
- Added support for JSON Schema draft 2019-09 and 2020-12.
- Internal dependency version bumps.
- Test suite cleanup.

### Fixed

- Fixed the `time` format inference in `FormatInferrers.dateTime()` to better adhere to the JSON Schema standard. It is trivial to restore the original behavior with a custom implementation of `FormatInferrer`.

## [0.1.5] - 2022-03-10

### Changed

- Switched to Maven Wrapper.
- Internal dependency version bumps.

## [0.1.4] - 2021-07-21

## [0.1.3] - 2021-05-21

### Changed

- Internal dependency version bumps.

### Fixed

- Fixed errors when building for JDK 11 and 12. See [#6](https://github.com/saasquatch/json-schema-inferrer/pull/6).

## [0.1.2] - 2020-03-12

### Changed

- Internal dependency version bumps.

## [0.1.1] - 2020-02-27

[Unreleased]: https://github.com/saasquatch/json-schema-inferrer/compare/0.2.1...HEAD

[0.2.1]: https://github.com/saasquatch/json-schema-inferrer/compare/0.2.0...0.2.1

[0.2.0]: https://github.com/saasquatch/json-schema-inferrer/compare/0.1.5...0.2.0

[0.1.5]: https://github.com/saasquatch/json-schema-inferrer/compare/0.1.4...0.1.5

[0.1.4]: https://github.com/saasquatch/json-schema-inferrer/compare/0.1.3...0.1.4

[0.1.3]: https://github.com/saasquatch/json-schema-inferrer/compare/0.1.2...0.1.3

[0.1.2]: https://github.com/saasquatch/json-schema-inferrer/compare/0.1.1...0.1.2

[0.1.1]: https://github.com/saasquatch/json-schema-inferrer/releases/tag/0.1.1
