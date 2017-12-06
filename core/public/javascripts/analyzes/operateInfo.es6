$(() => {

  Highcharts.Pointer.prototype.reset = function () {
    return undefined;
  };

  Highcharts.Point.prototype.highlight = function (event) {
    this.onMouseOver(event); // Show the hover marker
    this.series.chart.tooltip.refresh(this); // Show the tooltip
    this.series.chart.xAxis[0].drawCrosshair(event, this); // Show the crosshair
  };

  function syncExtremes (e) {
    let thisChart = this.chart;
    if (e.trigger !== 'syncExtremes') { // Prevent feedback loop
      Highcharts.each(Highcharts.charts, function (chart) {
        if (chart !== thisChart) {
          if (chart.xAxis[0].setExtremes) { // It is null while updating
            chart.xAxis[0].setExtremes(e.min, e.max, undefined, false, {trigger: 'syncExtremes'});
          }
        }
      });
    }
  }

  $("#searchBtn").click(function () {
    $('#postVal').val("11UNMIC5P-2A5FTUK,690494323893|A_UK|2");
    ajaxSessionLine();
    ajaxTurnOverLine();

    setTimeout(function () {
      $('#a_ss,#a_turn').bind('mousemove touchmove touchstart', function (e) {
        let currentChart = $(this).highcharts(), chart, point, i,
        event = currentChart.pointer.normalize(e.originalEvent),
        currentPoint = currentChart.series[0].searchPoint(event, true);
        if (point) {
          point.highlight(e);
        }
        if (currentPoint !== undefined) {
          for(i = 0; i < Highcharts.charts.length; i = i + 1) {
            chart = Highcharts.charts[i];
            if (chart !== currentChart) {
              point = chart.series[0].searchPoint({
                chartX: currentPoint.plotX + chart.plotLeft,
                chartY: currentPoint.plotY + chart.plotTop
              }, true);
              if (point) {
                point.highlight(e);
              }
            }
          }
        }

      });
    }, 3000);
  });

  $('#a_turn,#a_ss').bind('mouseleave', function (e) {
    Highcharts.each(Highcharts.charts, function (chart) {
      let event = chart.pointer.normalize(e.originalEvent);
      let point = chart.series[0].searchPoint(event, true);

      if (point) {
        point.onMouseOut();
        chart.tooltip.hide(point);
        chart.xAxis[0].hideCrosshair();
      }
    });
  });

  $("#basic").on('ajaxFresh', '#a_units, #a_turn, #a_ss', function (e, headName, yName, plotEvents, noDataDisplayMessage) {
    let $div = $(this);
    LoadMask.mask($div);
    $.post("/analyzes/" + $div.data('method'), $('#click_param').serialize(), function (r) {
      if (r['series'].length != 0) {
        Highcharts.chart($div.attr("id"), {
          credits: {
            text: "EasyAcc",
            href: ''
          },
          title: {
            text: headName
          },
          legend: {
            enabled: true
          },
          navigator: {
            enabled: true
          },
          scrollbar: {
            enabled: false
          },
          rangeSelector: {
            enabled: true,
            button: [{
              type: 'week',
              count: 1,
              text: '1w'
            }, {
              type: 'month',
              count: 1,
              text: '1m'
            }],
            selected: 1
          },
          xAxis: {
            crosshair: true,
            type: 'datetime',
            events: {
              setExtremes: syncExtremes
            }
          },
          yAxis: {min: 0},
          plotOptions: {
            series: {
              cursor: "pointer",
              events: plotEvents
            }
          },
          tooltip: {
            shared: true,
            formatter: function () {
              let s = "<b>" + Highcharts.dateFormat('%Y-%m-%d', this.x) + "</b><br>";
              this.points.forEach(function (point) {
                let totalY = point.series.yData.reduce(function (a, b) {
                  return a + b;
                });
                s += "<span style='color:" + point.series.color + "'>" + point.series.name + "</span>:<b>" + point.y + " (" + totalY + ")</b><br>";
              });
              return s;
            },
            xDateFormat: '%Y-%m-%d'
          },
          series: r['series']
        });
      } else {
        $div.html(noDataDisplayMessage);
      }
      LoadMask.unmask($div);
    });
  });

  // 绘制 Session 的曲线
  function ajaxSessionLine () {
    // 无数据提示
    let noDataDisplay = '<div class="alert alert-success"><h3 style="text-align:center">双击查看 Selling 的 PageView &' + ' Session</h3></div>';
    $("#a_ss").trigger("ajaxFresh", ['Selling[' + $('#postVal').val() + '] SS', "Session && PV", {}, noDataDisplay]);
  }

  // 转换率的曲线
  function ajaxTurnOverLine () {
    //无数据提示
    let noDataDisplay = '<div class="alert alert-success"><h3 style="text-align:center">请双击需要查看的 Selling' + ' 查看转化率</h3></div>';
    $("#a_turn").trigger("ajaxFresh", ['Selling[' + $('#postVal').val() + '] 转化率', "转化率", {}, noDataDisplay]);
  }

});




