import React from 'react';
import ReactMarkdown from 'react-markdown';
import rehypeHighlight from 'rehype-highlight';

export type MarkdownRendererProps = {
  text: string;
  className?: string;
};

// Error boundary to prevent markdown rendering errors from crashing the app
class MarkdownErrorBoundary extends React.Component<
  React.PropsWithChildren<MarkdownRendererProps>,
  { hasError: boolean }
> {
  state = { hasError: false };

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidUpdate(prevProps: MarkdownRendererProps) {
    if (prevProps.text !== this.props.text && this.state.hasError) {
      this.setState({ hasError: false });
    }
  }

  render() {
    if (this.state.hasError) {
      return (
        <pre className={this.props.className}>{this.props.text}</pre>
      );
    }
    return this.props.children as React.ReactElement;
  }
}

const MarkdownRenderer: React.FC<MarkdownRendererProps> = ({ text, className }) => (
  <MarkdownErrorBoundary text={text} className={className}>
    <ReactMarkdown
      rehypePlugins={[rehypeHighlight]}
      className={className}
    >
      {text}
    </ReactMarkdown>
  </MarkdownErrorBoundary>
);

export default MarkdownRenderer;
