import { toggleRenderComponent } from "./entry-helpers";

const componentName = 'Tidy TODOs'
const codeBlockUID = 'roam-render-tidy-todos-cljs';
const cssBlockUID = 'roam-render-tidy-todos-css';
const renderString = `{{[[roam/render]]:((${codeBlockUID}))`;
const replacementString = '{{tidy-todos}}';
const version = 'v9';
const titleblockUID = 'roam-render-tidy-todos';
const cssBlockParentUID = 'tidy-todos-css-parent';

function onload({extensionAPI}) {
  const panelConfig = {
    tabTitle: componentName,
    settings: [
        // {id:		  "strikethrough",
        //   name:		"Strikethrough DONE tasks",
        //   description: "Adds CSS to strike through DONE tasks",
        //   action:	  {type:	 "switch",
        //                 onChange: (evt) => { 
        //                   // toggleStrikethroughCSS(evt.target.checked); 
        //                   console.log("toggle strikethrough CSS!", evt.target.checked); }}}
    ]
  };

  extensionAPI.settings.panel.create(panelConfig);

  if (!roamAlphaAPI.data.pull("[*]", [":block/uid", titleblockUID])) {
    // component hasn't been loaded so we add it to the graph
    toggleRenderComponent(true, titleblockUID, cssBlockParentUID, version, renderString, replacementString, cssBlockUID, codeBlockUID, componentName)
  }

  console.log(`load ${componentName} plugin`)
}

function onunload() {
  console.log(`unload ${componentName} plugin`)
  toggleRenderComponent(false, titleblockUID, cssBlockParentUID, version, renderString, replacementString, cssBlockUID, codeBlockUID, componentName)
}

export default {
onload,
onunload
};
