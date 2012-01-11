$(function() {
    $('.whsInput').overlay();
    var whsForm = $('#add_whs_form');

    $('#addWhBtn').click(function() {
        var params = {};
        whsForm.find(':input').map(function() {
            var o = $(this);
            if(!o.attr('name'))return false;
            params[o.attr('name')] = o.val();
        });
        alert(JSON.stringify(params));
    });
});