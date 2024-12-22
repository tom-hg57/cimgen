import chevron
import shutil
from pathlib import Path
from importlib.resources import files
from typing import Callable


# Setup called only once: make output directory, create base class, create profile class, etc.
# This just makes sure we have somewhere to write the classes.
# cgmes_profile_details contains index, names and uris for each profile.
# We don't use that here because we aren't exporting into
# separate profiles.
def setup(output_path: str, cgmes_profile_details: list[dict], cim_namespace: str) -> None:  # NOSONAR
    source_dir = Path(__file__).parent
    dest_dir = Path(output_path)
    for file in dest_dir.glob("**/*.java"):
        file.unlink()
    # Add all hardcoded utils and create parent dir
    for file in source_dir.glob("**/*.java"):
        dest_file = dest_dir / file.relative_to(source_dir)
        dest_file.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy(file, dest_file)


# These are the files that are used to generate the java files.
class_template_file = {"filename": "class_template.mustache", "ext": ".java"}
enum_template_file = {"filename": "enum_template.mustache", "ext": ".java"}

partials = {
    "label_without_keyword": "{{#lang_pack.label_without_keyword}}{{label}}{{/lang_pack.label_without_keyword}}",
}


def get_base_class() -> str:
    return "BaseClass"


def get_class_location(class_name: str, class_map: dict, version: str) -> str:  # NOSONAR
    return ""


# This is the function that runs the template.
def run_template(output_path: str, class_details: dict) -> None:
    if class_details["is_a_primitive_class"] or class_details["is_a_datatype_class"]:
        return
    if class_details["is_an_enum_class"]:
        template = enum_template_file
    else:
        template = class_template_file
    class_file = Path(output_path) / (class_details["class_name"] + template["ext"])
    _write_templated_file(class_file, class_details, template["filename"])


def _write_templated_file(class_file: Path, class_details: dict, template_filename: str) -> None:
    with class_file.open("w", encoding="utf-8") as file:
        templates = files("cimgen.languages.javadb.templates")
        with templates.joinpath(template_filename).open(encoding="utf-8") as f:
            args = {
                "data": class_details,
                "template": f,
                "partials_dict": partials,
            }
            output = chevron.render(**args)
        file.write(output)


# This function just allows us to avoid declaring a variable called 'switch',
# which is in the definition of the ExcBBC class.
def label_without_keyword(text: str, render: Callable[[str], str]) -> str:
    label = render(text)
    return _get_label_without_keyword(label)


def _get_label_without_keyword(label: str) -> str:
    if label == "switch":
        return "_switch"
    return label


def resolve_headers(path: str, version: str) -> None:  # NOSONAR
    pass
