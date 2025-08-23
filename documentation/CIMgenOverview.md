# Overview of projects based on CIMgen

| Project                 | cpp      | java     | javascript | modernpython | python    |
|:------------------------|:--------:|:--------:|:----------:|:------------:|:---------:|
| sogno-platform          | [libcimpp](https://github.com/sogno-platform/libcimpp) | [cim4j](https://github.com/sogno-platform/cim4j) | [pintura](https://github.com/sogno-platform/pintura) | - | [cimpy](https://github.com/sogno-platform/cimpy) |
| Other github            |          |          |            | [alliander-opensource/pycgmes](https://github.com/alliander-opensource/pycgmes) | |
| Release packages        | deb, rpm | jar      | docker     | pip          | pip       |
| Current release         | [GitHub](https://github.com/sogno-platform/libcimpp/releases/latest) | [GitHub](https://github.com/sogno-platform/cim4j/releases/latest) | [Docker Hub](https://hub.docker.com/r/sogno/pintura) | [PyPI](https://pypi.org/project/pycgmes) | [PyPI](https://pypi.org/project/cimpy) |
| **Workflows**           |          |          |            |              |           |
| CIMgen upgrade workflow | -        | -        | -          | -            | -         |
| Check workflow          | -        | -        | -          | [build](https://github.com/alliander-opensource/pycgmes/actions/workflows/build.yaml) | [pre-commit](https://github.com/sogno-platform/cimpy/actions/workflows/pre-commit.yaml) |
| Build workflow          | [build-src](https://github.com/sogno-platform/libcimpp/actions/workflows/build-src.yml) | [build](https://github.com/sogno-platform/cim4j/blob/main/.github/workflows/build.yml) | - | [build](https://github.com/alliander-opensource/pycgmes/actions/workflows/build.yaml) | [test](https://github.com/sogno-platform/cimpy/actions/workflows/test.yaml) |
| Test workflow           | [build-src](https://github.com/sogno-platform/libcimpp/actions/workflows/build-src.yml) | [build](https://github.com/sogno-platform/cim4j/blob/main/.github/workflows/build.yml) | - | [build](https://github.com/alliander-opensource/pycgmes/actions/workflows/build.yaml) | [test](https://github.com/sogno-platform/cimpy/actions/workflows/test.yaml) |
| Docs workflow           | [build-doc](https://github.com/sogno-platform/libcimpp/actions/workflows/build-doc.yml) | - | [pages](https://github.com/sogno-platform/pintura/actions/workflows/pages.yaml) | - | [docs](https://github.com/sogno-platform/cimpy/actions/workflows/docs.yaml) |
| Release workflow        | [release](https://github.com/sogno-platform/libcimpp/actions/workflows/release.yml) | [release](https://github.com/sogno-platform/cim4j/blob/main/.github/workflows/release.yml) | [release](https://github.com/sogno-platform/pintura/actions/workflows/release.yaml) | [deploy](https://github.com/alliander-opensource/pycgmes/actions/workflows/deploy.yaml) | - |
| **CIM features**        |          |          |            |              |           |
| Multi version           | x        | x        | -          | -            | -         |
| CGMES_2.4.13_18DEC2013  | x        | -        | -          | -            | -         |
| CGMES_2.4.15_16FEB2016  | x        | -        | -          | -            | -         |
| CGMES_2.4.15_27JAN2020  | x        | x        | x          | -            | x         |
| CGMES_3.0.0             | x        | x        | -          | x            | -         |
| Profiles                | x        | x        | x          | x            | -         |
| Custom profiles         | x        | x        | -          | x            | -         |
| Custom namespaces       | x        | x        | -          | x            | -         |
