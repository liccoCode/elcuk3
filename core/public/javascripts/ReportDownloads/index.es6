$(() => {

  $("#reports").on("click", "#repeatcalculate", function (r) {
    if (!confirm("确认重新计算?")) {
      return false;
    }
    return $.ajax($(this).data('url'), {
      type: 'POST',
      data: ""
    }).done(function (r) {
      let msg;
      msg = r.flag === true ? "" + r.message : r.message;
      return alert(msg);
    }).fail(function (r) {
      return alert(r.responseText);
    });
  });

  let resultMap = {};
  $("#cooperator_input").typeahead({
    source: (query, process) => {
      $.get('/cooperators/findSameCooperator', {name: $("#cooperator_input").val()}, function (c) {
        $("#cooperator_id").val("")
        resultMap = c;
        let result = _.map(c, function (n) {
          return n.split("-")[0];
        });
        process(result)
      });
    },

    updater: (item) => {
      let coo;
      coo = _.find(resultMap, function (n) {
        if (n.split("-")[0] === item) {
          return n;
        }
      });
      $("#cooperator_id").val(coo.split("-")[1]);
      return item;
    }
  });

});