$(() => {
  $('#cci').click(() => {
    $.post('/application/clearCache', {}, (r) => {
      if (r.flag) {
        alert('清理首页缓存成功');
      } else {
        alert('清理失败.');
      }
    });
  });

  $.post("/application/perDayOrderNum", function (r) {
    let title = r.title == undefined ? r["series"][0]["name"] : r.title;
    Highcharts.chart('pieChart1', {
      credits: {
        text: "EasyAcc",
        href: ""
      },
      chart: {
        plotBackgroundColor: null,
        plotBorderWidth: null,
        plotShadow: false,
        type: 'pie'
      },
      title: {
        text: title
      },
      tooltip: {
        pointFormat: '{point.y}/{point.total}: <b>{point.percentage:.1f}%</b>'
      },
      plotOptions: {
        pie: {
          allowPointSelect: true,
          cursor: 'pointer',
          dataLabels: {
            enabled: false
          },
          showInLegend: true,
          events: {
            click: function (event) {
              refreshTopFive(event.point.name);
            },
            afterAnimate: function (event) {
              if ($("#brandname").val() !== 'EASYACC') {
                refreshTopFive(r["highestMarket"]);
              }
            }
          }
        }
      },
      series: r["series"]
    });
  });

  $.post("/application/ajaxUnit", function (r) {
    Highcharts.chart("totalAjaxUnitDiv", {
      credits: {
        text: 'EasyAcc',
        href: ''
      },
      title: {
        text: "All Unit Order"
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
        buttons: [{
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
        type: 'datetime',
        yAxis: {min: 0}
      },
      tooltip: {
        shared: true,
        crosshairs: true,
        xDateFormat: '%Y-%m-%d'
      },
      series: r['series']
    });
  });

  function refreshTopFive (market) {
    let div = $("#progress_by_market");
    div.mask();
    div.load("/application/topTenSkuByMarket", {market: market}, function () {
      div.unmask();
      let fid = $("#progress_by_market").find("div").data("fid");
      getSales(fid);
    });
  }

  $(document).on("click", "a[name='sku_href']", function (r) {
    getSales($(this).data("fid"));
  });

  function getSales (fid) {
    let sid = fid;
    $.post("/application/ajaxUnit", {sid: sid}, function (r) {
      Highcharts.chart("ajaxUnitDiv", {
        credits: {
          text: 'EasyAcc',
          href: ''
        },
        title: {
          text: sid
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
          buttons: [{
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
          type: 'datetime',
          yAxis: {min: 0}
        },
        tooltip: {
          shared: true,
          crosshairs: true,
          xDateFormat: '%Y-%m-%d'
        },
        series: r['series']
      });
    });
  }

  let libColor = 'rgba(240,190,50,0.80)', grnColor = 'rgba(90,200,90,0.80)';

  function getMap () {
    Highcharts.mapChart('ajaxMap', {
      chart: {
        borderWidth: 0
      },
      credits: {
        text: 'EasyAcc',
        href: ''
      },
      colorAxis: {
        dataClasses: [{
          from: 2000,
          to: 10000,
          color: 'rgba(244,91,91,0.5)',
          name: '2000+'
        }, {
          from: 1000,
          to: 2000,
          color: 'rgba(124,181,236,0.5)',
          name: '1000-2000'
        }, {
          from: 500,
          to: 1000,
          name: '500-1000',
          color: libColor
        }, {
          from: 1000,
          to: 10000,
          name: '0-500',
          color: grnColor
        }]
      },

      colors: ['rgba(19,64,117,0.05)', 'rgba(19,64,117,0.2)', 'rgba(19,64,117,0.4)',
        'rgba(19,64,117,0.5)', 'rgba(19,64,117,0.6)', 'rgba(19,64,117,0.8)', 'rgba(19,64,117,1)'],

      title: {
        text: 'order num by country'
      },

      mapNavigation: {
        enabled: true
      },

      legend: {
        title: {
          text: 'order num',
          style: {
            color: (Highcharts.theme && Highcharts.theme.textColor) || 'black'
          }
        },
        align: 'left',
        verticalAlign: 'bottom',
        floating: true,
        layout: 'vertical',
        valueDecimals: 0,
        backgroundColor: (Highcharts.theme && Highcharts.theme.legendBackgroundColor) || 'rgba(255, 255, 255, 0.85)',
        symbolRadius: 0,
        symbolHeight: 14
      },

      series: [{
        data: [{
          code: "DE",
          value: 4000,
          name: "Germany"
        }, {
          code: "GB",
          value: 1000,
          name: "United Kingdom"
        }, {
          code: "FR",
          value: 500,
          name: "France"
        }, {
          code: "IT",
          value: 200,
          name: "Italy"
        }, {
          code: "ES",
          value: 10,
          name: "Spain"
        }, {
          code: "JP",
          value: 10,
          name: "Japan"
        }, {
          code: "US",
          value: 1200,
          name: "United States"
        }, {
          code: "CA",
          value: 10,
          name: "Canada"
        }],
        mapData: Highcharts.maps['custom/world'],
        joinBy: ['iso-a2', 'code'],
        animation: true,
        name: 'Order Nums.',
        states: {
          hover: {
            color: '#a4edba'
          }
        },
        tooltip: {
          valueSuffix: ''
        },
        shadow: false
      }]
    });
  }

  if ($("#brandname").val() == "EASYACC") {
    getMap();
  }

});
