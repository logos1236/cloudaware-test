jQuery(document).ready(function () {
//=== Pagination
    $("body").on("change", ".service-pagination-form select", function (event) {
        event.stopPropagation();

        var _this = $(this);
        var _this_form = _this.closest("form");

        $(this).closest("form").submit();
        return false;
    });
});
