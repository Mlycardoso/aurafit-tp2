import { flavor, LojaState, ShopProduct, weight } from './loja-state';

const product: ShopProduct = {
  id: 1,
  nome: 'Whey Chocolate 900g',
  descricao: null,
  preco: 99.9,
  estoque: 2,
  ativo: true,
  categoria: { id: 1, nome: 'Whey concentrado', descricao: null, ativo: true },
};
describe('LojaState', () => {
  beforeEach(() => localStorage.clear());
  it('adiciona, altera quantidades e remove sem ultrapassar o estoque', () => {
    const state = new LojaState();
    state.add(product);
    state.add(product);
    expect(state.count()).toBe(2);
    state.add(product);
    expect(state.count()).toBe(2);
    state.quantity(1, 1, 2);
    expect(state.count()).toBe(1);
    state.quantity(1, -3, 2);
    expect(state.count()).toBe(1);
    state.remove(1);
    expect(state.count()).toBe(0);
  });
  it('rejeita produto inativo e quantidade fracionada', () => {
    const state = new LojaState();
    state.add({ ...product, ativo: false });
    state.add(product, 1.5);
    expect(state.count()).toBe(0);
  });
  it('restaura carrinho e favoritos sem salvar dados pessoais', () => {
    const state = new LojaState();
    state.add(product);
    state.favorite(1);
    const restored = new LojaState();
    expect(restored.cart()).toEqual([{ id: 1, quantity: 1 }]);
    expect(restored.favorites()).toEqual([1]);
    restored.favorite(1);
    expect(restored.favorites()).toEqual([]);
  });
  it('ignora armazenamento corrompido e entradas invalidas', () => {
    localStorage.setItem('aurafit.cart.v1', 'invalid');
    expect(new LojaState().cart()).toEqual([]);
    localStorage.setItem('aurafit.cart.v1', '[null,{"id":1,"quantity":-1},{"id":2,"quantity":3}]');
    expect(new LojaState().cart()).toEqual([{ id: 2, quantity: 3 }]);
  });
  it('extrai apenas sabor e peso presentes no cadastro', () => {
    expect(flavor(product)).toBe('Chocolate');
    expect(weight(product)).toBe('900g');
    expect(flavor({ ...product, nome: 'Whey' })).toBe('');
  });
});
