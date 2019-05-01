import React, {Component, PropTypes} from 'react';
import {Platform, View, requireNativeComponent} from 'react-native';
import {WebView} from 'react-native-webview';

export default class CustomWebView extends Component {
	static propTypes = WebView.propTypes;

	constructor( props: P, context: any ) {
		super( props, context );
	}

	render() {

		if (Platform.OS == 'android') {
			return (<WebView {...this.props} nativeConfig={{component: RCTCustomWebView}} />);
		}
		else {
			return (<WebView {...this.props} />);
		}

	}
}


const RCTCustomWebView = (Platform.OS !== 'android') ? null : requireNativeComponent(
	'RCTFSWebView',
	CustomWebView,
	WebView.extraNativeComponentConfig
);
