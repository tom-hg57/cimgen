import chevron
import logging
import shutil
from pathlib import Path
from importlib.resources import files
from typing import Callable

logger = logging.getLogger(__name__)


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
classlist_template = {"filename": "classlist_template.mustache", "ext": ".java"}

partials = {
    "label_without_keyword": "{{#lang_pack.label_without_keyword}}{{label}}{{/lang_pack.label_without_keyword}}",
}


def get_base_class() -> str:
    return "BaseClass"


def get_class_location(class_name: str, class_map: dict, version: str) -> str:  # NOSONAR
    return ""


# This is the function that runs the template.
def run_template(output_path: str, class_details: dict) -> None:

    # Add some class infos
    special_table_name = _special_table_name(class_details["class_name"])
    if special_table_name:
        class_details["special_table_name"] = special_table_name

    # Add some attribute infos
    for attribute in class_details["attributes"]:
        if _attribute_is_primitive_string(attribute) and attribute["attribute_class"] != "String":
            attribute["primitive_java_type"] = "String"
        special_column_name = _special_column_name(attribute["label"])
        if special_column_name:
            attribute["special_column_name"] = special_column_name
        attribute["is_really_used"] = "true" if _attribute_is_really_used(attribute) else ""

    if _filter_cim_classes(class_details):
        return
    if class_details["is_a_primitive_class"] or class_details["is_a_datatype_class"]:
        return
    if class_details["is_an_enum_class"]:
        template = enum_template_file
        class_category = "types"
    else:
        template = class_template_file
        class_category = ""
    class_file = Path(output_path) / class_category / (class_details["class_name"] + template["ext"])
    _write_templated_file(class_file, class_details, template["filename"])


def _write_templated_file(class_file: Path, class_details: dict, template_filename: str) -> None:
    class_file.parent.mkdir(parents=True, exist_ok=True)
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


def _special_table_name(class_name: str) -> str | None:
    """Get the name of the database table if different from class name.

    Some class names are not allowed as name of a database table.

    :param class_name:  Original class name
    :return:            Table name or None if no special table name needed
    """
    if class_name == "Limit":
        return "_Limit"
    return None


def _special_column_name(label: str) -> str | None:
    """Get the name of the database column if different from label.

    Some label names are not allowed as name of a database column.

    :param label:  Original label
    :return:       Column name or None if no special column name needed
    """
    if label == "value":
        return "_value"
    return None


def _attribute_is_primitive_string(attribute: dict) -> bool:
    """Check if the attribute is a primitive attribute that is used like a string (Date, MonthDay etc).

    :param attribute: Dictionary with information about an attribute.
    :return:          Attribute is a primitive string?
    """
    return attribute["is_primitive_attribute"] and (
        attribute["attribute_class"] not in ("Float", "Decimal", "Integer", "Boolean")
    )


def _filter_cim_classes(class_details: dict) -> bool:
    """Filter out all cim classes that are not in cim_class_filter_list.txt

    If cim_class_filter_list.txt don't exist nothing is filtered.
    If cim_class_filter_list.txt exists only classes in the list should be created.
    In these classes the attributes with not created attribute_classes are filtered out.

    :param class_details: Dictionary with information about a class.
    :return:              True = class is filtered out
                          False = class not filtered out, but attributes in class_details are filtered
    """
    source_dir = Path(__file__).parent
    filter_path = source_dir / "cim_class_filter_list.txt"
    if filter_path.exists():
        classes = set(filter_path.read_text().split())

        # Check if class is filtered out
        if not _class_ok(class_details, classes):
            # Check for missing super classes in cim_class_filter_list.txt
            if not classes.intersection(class_details["subclasses"]):
                return True
            logger.error("Superclass '{}' missing in cim_class_filter_list.txt".format(class_details["class_name"]))

        # Filter attributes in class_details
        class_details["attributes"] = list(
            filter(lambda attr: _attribute_ok(attr, classes), class_details["attributes"])
        )
    return False


def _class_ok(class_details: dict, classes: set[str]) -> bool:
    """Check if the class is not filtered out.

    :param class_details: Dictionary with information about a class.
    :param classes:       Set of classes that should be created.
    :return:              Class is ok?
    """
    return class_details["class_name"] in classes or class_details["is_an_enum_class"]


def _attribute_ok(attribute: dict, classes: set[str]) -> bool:
    """Check if the attribute is not filtered out.

    :param attribute: Dictionary with information about an attribute.
    :param classes:   Set of classes that should be created.
    :return:          Attribute is ok?
    """
    if attribute["attribute_class"] in classes:
        return True
    if attribute["is_primitive_attribute"] or attribute["is_datatype_attribute"] or attribute["is_enum_attribute"]:
        return True
    return False


def _attribute_is_really_used(attribute: dict) -> bool:
    """Check if the attribute is really used.

    List attributes couldn't be used as OneToMany links. Instead of that the inverse attribute is linked ManyToOne.

    :param attribute: Dictionary with information about an attribute.
    :return:          Attribute is really used?
    """
    if attribute["is_used"] and not attribute["is_list_attribute"]:
        return True
    return attribute["is_class_attribute_with_inverse_list"]


# The code below this line is used after the main cim_generate phase to generate CIMClassMap.java.

class_blacklist = [
    "BaseClass",
    "CimModel",
    "CimClassMap",
    "Logging",
]


def _create_classlist_file(
    directory: Path, classlist_filename: str, template_filename: str, blacklist: list[str]
) -> None:
    classes = []
    for file in sorted(directory.glob("*.java"), key=lambda f: f.stem):
        class_name = file.stem
        if class_name not in blacklist:
            classes.append(class_name)
    _write_templated_file(directory / classlist_filename, {"classes": classes}, template_filename)


def resolve_headers(path: str, version: str) -> None:  # NOSONAR
    _create_classlist_file(
        Path(path),
        "CimClassMap" + classlist_template["ext"],
        classlist_template["filename"],
        class_blacklist,
    )
