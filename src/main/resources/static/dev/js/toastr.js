function toastrPrintList(message_list, type) {
    if (typeof message_list !== 'undefined') {
        $.each(message_list, function (key, input) {
            if (input.text != '') {
                if (type == 'success') {
                    toastr.success(input.text);
                } else if (type == 'error') {
                    toastr.error(input.text);
                }
            }
        });
    } else {
        if (type == 'error') {
            toastr.error(window.toastr_error);
        }
    }
}