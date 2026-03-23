import 'package:jaspr/jaspr.dart';

@client
class FirstComponent extends StatelessComponent {
  @override
  Iterable<Component> build(BuildContext context) sync* {
    yield div([], []);
  }
}

@client
class SecondComponent extends StatelessComponent {
  @override
  Iterable<Component> build(BuildContext context) sync* {
    yield span([], []);
  }
}
