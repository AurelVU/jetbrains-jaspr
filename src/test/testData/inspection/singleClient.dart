import 'package:jaspr/jaspr.dart';

@client
class MyComponent extends StatelessComponent {
  @override
  Iterable<Component> build(BuildContext context) sync* {
    yield div([], []);
  }
}
