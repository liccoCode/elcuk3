$(() => {

  let $search = $("#quickSearchInput");
  $search.typeahead({
    source: (query, process) => {
      let name = $search.val();
      $.get($search.data("url"), {search: name}, function (c) {
        process(c);
      });
    },

    updater: (item) => {
      return item;
    }
  });

  $("#quickAddByEdit").click(function () {
    LoadMask.mask();
    $.post($(this).data("url"), {
      name: $("#quickSearchInput").val(),
      id: $(this).data("id")
    }, function (r) {
      if (r) {
        window.location.reload();
      }
    });
  });

});