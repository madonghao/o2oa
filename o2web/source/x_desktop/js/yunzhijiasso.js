layout = window.layout || {};
var locate = window.location;
layout.protocol = locate.protocol;
var href = locate.href;
if (href.indexOf("debugger") != -1) layout.debugger = true;
layout.desktop = layout;
layout.session = layout.session || {};
o2.addReady(function () {
    // o2.load(["../o2_lib/mootools/plugin/mBox.Notice.js", "../o2_lib/mootools/plugin/mBox.Tooltip.js"], { "sequence": true }, function () {

        MWF.loadLP("zh-cn");

        MWF.require("MWF.xDesktop.Layout", function () {
            MWF.require("MWF.xDesktop.Authentication", null, false);
            MWF.require("MWF.xDesktop.Common", null, false);

            (function () {
                layout.load = function () {
                    var uri = href.toURI();
                    var mode = uri.getData("mode");
                    var ticket = uri.getData("ticket");
                    var redirect = uri.getData("redirect");
                    MWF.require("MWF.xDesktop.Actions.RestActions", function () {
                        var action = new MWF.xDesktop.Actions.RestActions("", "x_organization_assemble_authentication", "");
                        action.getActions = function (actionCallback) {
                            this.actions = { "sso": { "uri": "/jaxrs/yunzhijia/ticket/{ticket}", "method": "GET" },
                                            "userdetail" : {"uri": "/jaxrs/qiyeweixin/update/person/detail/{code}", "method": "GET" } };
                            if (actionCallback) actionCallback();
                        };
                        action.invoke({
                            "name": "sso", "async": true, "parameter": { "ticket":  ticket}, "success": function (json) {
                                if (redirect) {
                                    history.replaceState(null, "page", redirect);
                                    redirect.toURI().go();
                                } else {
                                    history.replaceState(null, "page", "../x_desktop/appMobile.html?app=process.TaskCenter");
                                    "appMobile.html?app=process.TaskCenter".toURI().go();
                                }

                                //"appMobile.html?app=process.TaskCenter".toURI().go();
                                //window.loaction = "app.html?app=process.TaskCenter";
                            }.bind(this), "failure": function (xhr, text, error) {
                                document.id("layout").set("html", "<div>云之家单点异常！</div>")
                                //"appMobile.html?app=process.TaskCenter".toURI().go();
                                //window.loaction = "app.html?app=process.TaskCenter";
                            }.bind(this)
                        });
                    });
                    
                };

                

                // layout.notice = function (content, type, target, where, offset) {
                //     if (!where) where = { "x": "right", "y": "top" };
                //     if (!target) target = this.content;
                //     if (!type) type = "ok";
                //     var noticeTarget = target || $(document.body);
                //     var off = offset;
                //     if (!off) {
                //         off = {
                //             x: 10,
                //             y: where.y.toString().toLowerCase() == "bottom" ? 10 : 10
                //         };
                //     }

                //     new mBox.Notice({
                //         type: type,
                //         position: where,
                //         move: false,
                //         target: noticeTarget,
                //         delayClose: (type == "error") ? 10000 : 5000,
                //         offset: off,
                //         content: content
                //     });
                // };

                MWF.getJSON("res/config/config.json", function (config) {
                    if (config.proxyCenterEnable){
                        if (o2.typeOf(config.center)==="array"){
                            config.center.forEach(function(c){
                                c.port = window.location.port || 80;
                            });
                        }else{
                            config.port = window.location.port || 80;
                        }
                    }
                    layout.config = config;
                    MWF.xDesktop.getServiceAddress(layout.config, function (service, center) {
                        layout.serviceAddressList = service;
                        layout.centerServer = center;
                        layout.load();
                    }.bind(this));
                    //layout.getServiceAddress(function(){
                    //    layout.load();
                    //});
                });

            })();

        });
    // });
});
